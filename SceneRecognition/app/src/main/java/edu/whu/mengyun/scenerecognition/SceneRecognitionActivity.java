package edu.whu.mengyun.scenerecognition;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.provider.BigImageCardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.tzutalin.vision.visionrecognition.SceneClassifier;
import com.tzutalin.vision.visionrecognition.VisionClassifierCreator;
import com.tzutalin.vision.visionrecognition.VisionDetRet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SceneRecognitionActivity extends Activity {

    private final static String TAG = "RecognitionActivity";
    private SceneClassifier mClassifier;

    private Drawable drawable;//scene pic

    private MaterialListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_recognition);
        mListView = (MaterialListView) findViewById(R.id.material_listview_id);

        final String key = SceneCaptureFragment.KEY_IMGPATH;
        String imgPath = getIntent().getExtras().getString(key);

        if(!new File(imgPath).exists()){
            Toast.makeText(this, "No file path", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        } else{
            Log.d(TAG,"image path - " + imgPath);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bm = BitmapFactory.decodeFile(imgPath, options);
            drawable = new BitmapDrawable(getResources(), bm);
            Log.d(TAG, drawable.toString());
        }


        PredictTask task = new PredictTask();
        task.execute(imgPath);

        Card card = new Card.Builder(SceneRecognitionActivity.this)
                .withProvider(BigImageCardProvider.class)
                .setDescription("Input image")
                .setDrawable(drawable)
                .endConfig()
                .build();
        mListView.add(card);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mClassifier != null){
            mClassifier.deInit();
        }
    }


    // ==========================================================
    // Tasks inner class
    // ==========================================================
    private class PredictTask extends AsyncTask<String, Void, List<VisionDetRet>> {
        private ProgressDialog mmDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mmDialog = ProgressDialog.show(SceneRecognitionActivity.this, getString(R.string.dialog_wait),getString(R.string.dialog_scene_description), true);
        }

        @Override
        protected List<VisionDetRet> doInBackground(String... strings) {
            initCaffeMobile();
            Log.d(TAG, "iniCaffeMobile done");
            long startTime;
            long endTime;
            final String filePath = strings[0];
            List<VisionDetRet> rets = new ArrayList<>();
            Log.d(TAG, "PredictTask filePath:" + filePath);
            if (mClassifier != null) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                Log.d(TAG, "format:" + options.inPreferredConfig);
                Bitmap bitmapImg = BitmapFactory.decodeFile(filePath, options);
                startTime = System.currentTimeMillis();
                rets.addAll(mClassifier.classify(bitmapImg));

                endTime = System.currentTimeMillis();
                final double diffTime = (double) (endTime - startTime) / 1000;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SceneRecognitionActivity.this, "Take " + diffTime + " second", Toast.LENGTH_LONG).show();
                    }
                });
            }
            File beDeletedFile = new File(filePath);
            if (beDeletedFile.exists()) {
                beDeletedFile.delete();
            } else {
                Log.d(TAG, "file does not exist " + filePath);
            }
            return rets;
        }

        @Override
        protected void onPostExecute(List<VisionDetRet> rets) {
            super.onPostExecute(rets);
            if (mmDialog != null) {
                mmDialog.dismiss();
            }

            ArrayList<String> items = new ArrayList<>();
            for (VisionDetRet each : rets) {
                items.add("[" + each.getLabel() + "] Prob: " + each.getConfidence());
            }

            int count = 0;
            for (String item : items) {
                count++;
                //BigImageButtonCard bigImageButtonCard = new BigImageButtonCard();
                //BigImageCard bigImageCard = new BigImageCard();
                Card card = new Card.Builder(SceneRecognitionActivity.this)
                        .withProvider(BigImageCardProvider.class)
                        .setTitle("Top " + count)
                        .setDescription(item)
                        .endConfig()
                        .build();
                mListView.add(card);
            }
        }
    }
    // ==========================================================
    // Private methods
    // ==========================================================
    private void initCaffeMobile() {
        if (mClassifier == null) {
            try {
                mClassifier = VisionClassifierCreator.createSceneClassifier(getApplicationContext());
                Log.d(TAG, "Start Load model");
                // TODO : Fix it - initCaffeMobile()
                mClassifier.init(224,224);  // init once
                //mClassifier.init(drawable.getMinimumWidth(),drawable.getMinimumHeight());
                Log.d(TAG, "End Load model");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
