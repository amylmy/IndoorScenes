package com.sh1r0.caffe_android_lib;

/**
 * Created by shiro on 3/26/15.
 */
public class CaffeMobile {

    static {
        System.loadLibrary("caffe");
        System.loadLibrary("caffe_jni");
        android.util.Log.d("CaffeMobile", "library load success");
    }

    public  native void setNumThreads(int numThreads);

    public native void enableLog(boolean enabled);  // currently nonfunctional

    public native int loadModel(String modelPath, String weightsPath);  // required

    private native void setMeanWithMeanFile(String meanFile);

    private native void setMeanWithMeanValues(float[] meanValues);

    public native void setScale(float scale);

    public native float[] getConfidenceScore(String imgPath);

    public native int[] predictImage(String imgPath, int k);

    public native float[][] extractFeatures(String imgPath, String blobNames);

    public void setMean(float[] meanValues) {
        setMeanWithMeanValues(meanValues);
    }

    public void setMean(String meanFile) {
        setMeanWithMeanFile(meanFile);
    }

    public int[] predictImage(String imgPath) {
        return predictImage(imgPath, 1);
    }

}
