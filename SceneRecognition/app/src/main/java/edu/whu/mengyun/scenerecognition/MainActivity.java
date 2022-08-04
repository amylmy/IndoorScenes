package edu.whu.mengyun.scenerecognition;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sh1r0.caffe_android_lib.CaffeMobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

//    private static final int REQUEST_CODE_PERMISSION = 1;
//    private static final String TAG = "MainActivity";
//    // Storage Permissions
//    private static String[] PERMISSIONS_REQ = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA
//    };

    private Button loadButton;
    private Button previewButton;
    private Button chooseButton;

    private CaffeMobile caffeMobile;
    private ProgressDialog dialog;
    private Uri fileUri;
    private Bitmap bmp;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static String[] PLACES_CLASSES;

    String modelProtoName = "deploy_1.prototxt";
    String weightsFileName = "snapshot_iter_765280.caffemodel";
    String categoriesName = "categoryIndex_places205.csv";
    //String meanFileName = "places365CNN_mean.binaryproto";

    String modelDir = getRootPath() + "/caffe_mobile/places205/";
    String modelProtoPath = modelDir + modelProtoName;
    String modelBinaryPath = modelDir + weightsFileName;
    //synset is categories?
    String synsetPath = modelDir + categoriesName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getFragmentManager().beginTransaction()
//                        .replace(R.id.container, SceneCaptureFragment.newInstance())
//                        .addToBackStack("tag")
//                        .commit();

//        boolean avialbe_permission = true;
//        // For API 23+ you need to request the read/write permissions even if they are already in your manifest.
//        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        if (currentapiVersion >= Build.VERSION_CODES.M ) {
//            avialbe_permission = verifyPermissions(this);
//        }
//        if (avialbe_permission && null == savedInstanceState) {
////            getFragmentManager().beginTransaction()
////                    .replace(R.id.container, SceneCaptureFragment.newInstance())
////                    .commit();
//        }

        loadButton = (Button) findViewById(R.id.load_model_button_id);
        previewButton = (Button) findViewById(R.id.start_preview_button_id);
        chooseButton = (Button) findViewById(R.id.choose_pic_btn_id);

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainActivity.this, "Model is loading...", "Please wait...", true);
                // TODO: implement a splash screen(?
                caffeMobile = new CaffeMobile();
                caffeMobile.setNumThreads(4);
                caffeMobile.loadModel(modelProtoPath, modelBinaryPath);
                float[] meanValues = {105, 114, 116};
                caffeMobile.setMean(meanValues);
                PLACES_CLASSES = getSynsetsFromFile(synsetPath);
                if(PLACES_CLASSES!=null){
                    dialog.dismiss();
                    Log.d("MainActivity","caffe model load success");
                }
            }
        });

        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:add init
                initPrediction();
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
//                getFragmentManager().beginTransaction()
//                .replace(R.id.container, SceneCaptureFragment.newInstance())
//                .addToBackStack("tag")
//                .commit();
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initPrediction() {
        loadButton.setEnabled(false);
        previewButton.setEnabled(false);
//        btnCamera.setEnabled(false);
//        btnSelect.setEnabled(false);
//        tvLabel.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_SELECT) && resultCode == RESULT_OK) {
            String imgPath;

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imgPath = fileUri.getPath();
            } else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = MainActivity.this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            bmp = BitmapFactory.decodeFile(imgPath, options);
            Log.d("MainActivity", imgPath);
            Log.d("MainActivity", String.valueOf(bmp.getHeight()));
            Log.d("MainActivity", String.valueOf(bmp.getWidth()));

            dialog = ProgressDialog.show(MainActivity.this, "Predicting...", "Wait for one sec...", true);

//            CNNTask cnnTask = new CNNTask(MainActivity.this);
//            cnnTask.execute(imgPath);

        } else {
            previewButton.setEnabled(true);
            chooseButton.setEnabled(true);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


//    private static boolean verifyPermissions(Activity activity) {
//        // Check if we have write permission
//        int write_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int read_persmission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
//
//        if (write_permission != PackageManager.PERMISSION_GRANTED ||
//                read_persmission != PackageManager.PERMISSION_GRANTED ||
//                camera_permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_REQ,
//                    REQUEST_CODE_PERMISSION
//            );
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        try {
//            // Restart it after granting permission
//            if (requestCode == REQUEST_CODE_PERMISSION) {
//                finish();
//                startActivity(getIntent());
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
//        }
//    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startButton.setVisibility(View.VISIBLE);
//    }

    public static String getRootPath() {
        String path;
        //Check whether the SD Card exists.
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdDir = Environment.getExternalStorageDirectory();//root path of SD card
            path = sdDir.getPath();
        } else {
            //get the inner storage path
            File innerSD = Environment.getRootDirectory();
            path = innerSD.getPath();
        }
        return path;
    }

    private String[] getSynsetsFromFile(String synsetPath) {
        if (!TextUtils.isEmpty(synsetPath)){
            File file = new File(synsetPath);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line;
                List<String> lines = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String temp = line.substring(line.lastIndexOf("/") + 1);
                    lines.add(temp.split(" ")[0]);
                }
                return lines.toArray(new String[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Caffe-Android-Demo");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private class CNNTask extends AsyncTask<String, Void, Integer> {
        private CNNListener listener;
        private long startTime;

        public CNNTask(CNNListener listener) {
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            startTime = SystemClock.uptimeMillis();
            return caffeMobile.predictImage(strings[0])[0];
        }

        @Override
        protected void onPostExecute(Integer integer) {
            Log.i("MainActivity", String.format("elapsed wall time: %d ms", SystemClock.uptimeMillis() - startTime));
            listener.onTaskCompleted(integer);
            super.onPostExecute(integer);
        }
    }

}
