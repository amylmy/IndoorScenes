package cn.edu.whu.indoorscene.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author tanjiajie
 */
public class BitmapUtils {

    public static int minNum = -1;

    public static final String EXTERNAL_DIR_NAME = "multi_sensor_collector";
    private static final int REQUIRED_BITMAP_WIDTH = 1080;
    private static final int REQUIRED_BITMAP_HEIGHT = 1080;

    public static File getExternalDataDir() {
        return new File(
                Environment.getExternalStorageDirectory().getAbsoluteFile(),
                EXTERNAL_DIR_NAME);
    }

    public static int calculateInSampleSize(final InputStream bitmapStream, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(bitmapStream, null, options);
        return calculateInSampleSize(options, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        return calculateInSampleSize(options.outHeight, options.outWidth, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            final int halfHeight = rawHeight / 2;
            final int halfWidth = rawWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getBitmap(final InputStream bitmapStream) throws AccessResourceException {
        return getBitmap(bitmapStream, REQUIRED_BITMAP_WIDTH, REQUIRED_BITMAP_HEIGHT);
    }

    public static Bitmap getBitmap(final InputStream bitmapStream, final int reqWidth, final int reqHeight) throws AccessResourceException {
        if (bitmapStream == null)
            throw new AccessResourceException("Resource is undefined. The input should not be null.");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            bitmapStream.reset();
            BitmapFactory.decodeStream(bitmapStream, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmapStream.reset();
            return BitmapFactory.decodeStream(bitmapStream, null, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getStreamFromAssets(Context context, final String fileName) throws AccessResourceException {
        InputStream bitmapStream = null;
        try {
            AssetManager assetMgt = context.getAssets();
            return assetMgt.open(fileName);
        } catch (IOException e) {
            throw new AccessResourceException("Can not access to the resource.");
        }
    }

    public static Bitmap getBitmapFromAssets(Context context, final String fileName) throws AccessResourceException {
        try {
            InputStream bitmapStream = null;
            try {
                bitmapStream = getStreamFromAssets(context, fileName);
                return getBitmap(bitmapStream);
            } finally {
                if (bitmapStream != null)
                    bitmapStream.close();
            }
        } catch (IOException e) {
            throw new AccessResourceException("Can not access to the resource.");
        }
    }

/*    public static Bitmap getMapBitmapFromSDCard(Context context, final String path) {
        File dir = new File(getMapFileDirectory());
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (filename.toLowerCase(Locale.ENGLISH).contains(
                        fileName.toLowerCase(Locale.ENGLISH))) {
                    int index = filename.indexOf(".");
                    if (index != -1) {
                        String suffix = filename.substring(index);
                        if (filename.length() == suffix.length()
                                + fileName.length())
                            return true;
                    }
                }
                return false;
            }
        });
        if (files == null || files.length == 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(files[0].getAbsolutePath(), options);
    }

    public static Map<Integer, PointF> getLocationMapFromSDCard(Context context, String fileName) {
        Map<Integer, PointF> locationMap = new HashMap<Integer, PointF>();
        String filePath = getLocationMapDirectory() + fileName;
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                String[] items = line.split(",");
                int position = Integer.parseInt(items[0]);
                if (minNum == -1)
                    minNum = position;
                locationMap
                        .put(position, new PointF(Float.parseFloat(items[1]),
                                Float.parseFloat(items[2])));
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationMap;
    }

    public static void addCollectFinishedPoint(Context context, int position) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    context.openFileOutput("point.txt", Context.MODE_APPEND));
            writer.write(position + " ");
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getCollectFinishedPoint(Context context) {
        List<Integer> collectedPointList = new ArrayList<Integer>();
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(context.openFileInput("point.txt")));
            while ((line = bufferedReader.readLine()) != null) {
                String items[] = line.split(" ");
                for (String string : items) {
                    int number = Integer.parseInt(string);
                    collectedPointList.add(number);
                }

            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return collectedPointList;
    }

    public static void clearHistoryFiles(Context context) {
        context.deleteFile("point.txt");
    }

    public static void clearDataFiles(Context context) {
        boolean isSuccess = true;
        File file = new File(getWiFiDataDirectory());
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                Toast.makeText(context, "Ã»ÓÐÎÄ¼þ", Toast.LENGTH_SHORT).show();
                return;
            }
            for (File file2 : files) {
                if (!file2.delete())
                    isSuccess = false;
            }
        }
        File sampleFile = new File(getSampleSizeData());
        if (sampleFile.exists() && sampleFile.isDirectory()) {
            File[] files = sampleFile.listFiles();
            if (files.length == 0) {
                Toast.makeText(context, "Ã»ÓÐÎÄ¼þ", Toast.LENGTH_SHORT).show();
                return;
            }
            for (File file2 : files) {
                if (!file2.delete())
                    isSuccess = false;
            }
        }
        if (isSuccess)
            Toast.makeText(context, "É¾³ý³É¹¦", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "É¾³ýÊ§°Ü", Toast.LENGTH_SHORT).show();
    }

    public static String getConfigFilePath() {
        return getConfigFileDirectory() + "config.xml";
    }

    public static String getMapFileDirectory() {
        return Environment.getExternalStorageDirectory().getPath()
                + "/AP_WiFiCollecter/Map/";
    }

    public static String getConfigFileDirectory() {
        return Environment.getExternalStorageDirectory().getPath()
                + "/AP_WiFiCollecter/";
    }

    public static String getLocationMapDirectory() {
        return Environment.getExternalStorageDirectory().getPath()
                + "/AP_WiFiCollecter/";
    }

    public static String getWiFiDataDirectory() {
        return Environment.getExternalStorageDirectory().getPath()
                + "/AP_WiFiCollecter/WiFiData/";
    }

    public static void createDir() {
        File dataFile = new File(getWiFiDataDirectory());
        if (!dataFile.exists())
            dataFile.mkdirs();

        File sizeFile = new File(getSampleSizeData());
        if (!sizeFile.exists()) {
            sizeFile.mkdirs();
        }
    }

    *//**
     * Add by @author limkuan
     *//*
    public static String getSampleSizeData() {
        return Environment.getExternalStorageDirectory().getPath()
                + "/AP_WiFiCollecter/SampleSizeData/";
    }*/

}
