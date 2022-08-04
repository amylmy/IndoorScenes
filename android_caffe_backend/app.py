from PIL import Image
from flask import jsonify
import caffe
import cPickle
import cStringIO as StringIO
import datetime
import exifutil
import flask
import logging
import numpy as np
import optparse
import pandas as pd
import os
import time
import tornado.wsgi
import tornado.httpserver
import urllib
import werkzeug


REPO_DIRNAME = os.path.abspath(os.path.dirname(os.path.abspath(__file__)) + '/../..')
UPLOAD_FOLDER = 'pic_uploads'
ALLOWED_IMAGE_EXTENSIONS = set(['png', 'bmp', 'jpg', 'jpe', 'jpeg', 'gif'])


# Obtain the flask app object
app = flask.Flask(__name__)


@app.route('/')
def index():
    return flask.render_template('index.html', has_result=False)


@app.route('/classify', methods=['POST'])
def classify_upload():
    try:
        # Save file on disk.
        imagefile = flask.request.files['imagefile']
        filename_ = str(datetime.datetime.now()).replace(' ', '_') + \
            werkzeug.secure_filename(imagefile.filename)
        filename = os.path.join(UPLOAD_FOLDER, filename_)
        imagefile.save(filename)
        image = exifutil.open_oriented_im(filename)
    except Exception as err:
        logging.info('Uploaded image open error: %s', err)
        return jsonify(success=0)

    result = app.clf.classify(image)
    success = result[0]
    if success:
        labels = result[1]
        scores = result[2]
        return jsonify(success=1, labels=labels, scores=scores)
    else:
        return jsonify(success=0)


class ImagenetClassifier(object):
    default_args = {
    ###### change model path here
       'model_def_file': (
           '{}/models/bvlc_reference_caffenet/deploy.prototxt'.format(REPO_DIRNAME)),
       'pretrained_model_file': (
           '{}/models/bvlc_reference_caffenet/bvlc_reference_caffenet.caffemodel'.format(REPO_DIRNAME)),
       'mean_file': (
           '{}/python/caffe/imagenet/ilsvrc_2012_mean.npy'.format(REPO_DIRNAME)),
       'class_labels_file': (
           '{}/data/ilsvrc12/synset_words.txt'.format(REPO_DIRNAME)),
    }
    for key, val in default_args.iteritems():
        if not os.path.exists(val):
            raise Exception(
                "File for {} is missing. Should be at: {}".format(key, val))
    default_args['image_dim'] = 256
    default_args['raw_scale'] = 255.

    def __init__(self, model_def_file, pretrained_model_file, mean_file,
                 raw_scale, class_labels_file, image_dim, gpu_mode):
        if gpu_mode:
            caffe.set_mode_gpu()
        else:
            caffe.set_mode_cpu()

        self.net = caffe.Classifier(
            model_def_file, pretrained_model_file,
            image_dims=(image_dim, image_dim), raw_scale=raw_scale,
            mean=np.load(mean_file).mean(1).mean(1), channel_swap=(2, 1, 0)
        )

        with open(class_labels_file) as f:
            labels_df = pd.DataFrame([
                {
                    'synset_id': l.strip().split(' ')[0],
                    'name': ' '.join(l.strip().split(' ')[1:]).split(',')[0]
                }
                for l in f.readlines()
            ])
        try:
            # Use pandas new API, use old one if fails.
            self.labels = labels_df.sort_values(by='synset_id')['name'].values
        except AttributeError:
            self.labels = labels_df.sort('synset_id')['name'].values

    def classify(self, image):
        try:
            starttime = time.time()
            predict_scores = self.net.predict([image], oversample=True).flatten()
            endtime = time.time()

            indices = (-predict_scores).argsort()[:5]
            scores = ['%.5f' % predict_scores[i] for i in indices]
            predictions = [self.labels[i] for i in indices]

            # In addition to the prediction text, we will also produce
            # the length for the progress bar visualization.
            
            return (True, predictions, scores, '%.3f' % (endtime - starttime))
        except Exception as err:
            logging.info('Classification error: %s', err)
            return (False)


def start_tornado(app, port=8080):
    http_server = tornado.httpserver.HTTPServer(
        tornado.wsgi.WSGIContainer(app))
    http_server.listen(port)
    print("Tornado server starting on port {}".format(port))
    tornado.ioloop.IOLoop.instance().start()


def start_from_terminal(app):
    """
    Parse command line options and start the server.
    """
    parser = optparse.OptionParser()
    parser.add_option(
        '-d', '--debug',
        help="enable debug mode",
        action="store_true", default=False)
    parser.add_option(
        '-p', '--port',
        help="which port to serve content on",
        type='int', default=8080)
    parser.add_option(
        '-g', '--gpu',
        help="use gpu mode",
        action='store_true', default=False)

    opts, args = parser.parse_args()
    ImagenetClassifier.default_args.update({'gpu_mode': opts.gpu})

    # Initialize classifier + warm start by forward for allocation
    app.clf = ImagenetClassifier(**ImagenetClassifier.default_args)
    app.clf.net.forward()

    if opts.debug:
        app.run(debug=True, host='0.0.0.0', port=opts.port)
    else:
        start_tornado(app, opts.port)


if __name__ == '__main__':
    # logging.getLogger().setLevel(logging.INFO)
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)
    start_from_terminal(app)
