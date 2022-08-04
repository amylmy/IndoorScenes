package cn.edu.whu.indoorscene.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by tanjiajie on 2/15/17.
 */
public class FileUtils {

    public static final String EXTERNAL_APP_FOLDER = "multi_sensor_collector";
    public static final String DATA_COLLECTION_FOLDER = "data";
    public static final String DATA_ZIPPED_FOLDER = "zipped";
    public static final String DATA_MAP_FOLDER = "map";
    public static final String DATA_DELETED_FOLDER = "deleted_data";

    public static File getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    public static File getExternalAppDirectory() {
        return new File(getExternalStorageDirectory(), EXTERNAL_APP_FOLDER);
    }

    public static File getDataCollectionDirectory() {
        return new File(getExternalAppDirectory(), DATA_COLLECTION_FOLDER);
    }

    public static File getZippedDataDirectory() {
        return new File(getExternalAppDirectory(), DATA_ZIPPED_FOLDER);
    }

    public static File getDeletedDataDirectory() {
        return new File(getExternalAppDirectory(), DATA_DELETED_FOLDER);
    }

    public static File getMapDirectory() {
        return new File(getExternalAppDirectory(), DATA_MAP_FOLDER);
    }

    public static InputStream getStreamFromAssets(Context context, final String fileName) throws AccessResourceException {
        try {
            AssetManager assetMgt = context.getAssets();
            // only for test
            InputStream testInputStream = assetMgt.open("map/test.txt");
            try (BufferedReader testReader = new BufferedReader(new InputStreamReader(testInputStream))) {
                String testLine = testReader.readLine();
                System.out.println(testLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream inputStream = assetMgt.open(fileName);
            return inputStream;
        } catch (IOException e) {
            throw new AccessResourceException("Can not access to the resource.", e);
        }
    }

    public static InputStream getStreamFromAppDirectory(String relativePath) throws AccessResourceException {
        try {
            File file = new File(getExternalAppDirectory(), relativePath);
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new AccessResourceException("Can not access to the resource.", e);
        }
    }

    public static String getRelativePath(File file, File baseDir) {
        Stack<File> fileStack = new Stack<>();
        File mFile = file;
        fileStack.push(mFile);
        while ((mFile = mFile.getParentFile()) != null) {
            if (mFile.equals(baseDir))
                break;
            fileStack.push(mFile);
        }

        StringBuilder pathBuilder = new StringBuilder();
        while (!fileStack.isEmpty()) {
            pathBuilder.append(fileStack.pop().getName()).append(File.separator);
        }
        if (pathBuilder.length() > 0)
            pathBuilder.deleteCharAt(pathBuilder.length() - 1);
        return pathBuilder.toString();
    }

    /*
     *
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */

    public static boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     *
     * Zips a subfolder
     *
     */

    private static void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

//    public static File getFileFromSDCard(Context context, String fileName) {
//        File dir = new File(getMapFileDirectory());
//        File[] files = dir.listFiles(new FilenameFilter() {
//
//            @Override
//            public boolean accept(File dir, String filename) {
//                if (filename.toLowerCase(Locale.ENGLISH).contains(
//                        fileName.toLowerCase(Locale.ENGLISH))) {
//                    int index = filename.indexOf(".");
//                    if (index != -1) {
//                        String suffix = filename.substring(index);
//                        if (filename.length() == suffix.length()
//                                + fileName.length())
//                            return true;
//                    }
//                }
//                return false;
//            }
//        });
//        if (files == null || files.length == 0) {
//            return null;
//        }
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        return BitmapFactory.decodeFile(files[0].getAbsolutePath(), options);
//    }

    public static void main(String[] args) {
        File file = new File("/home/tanjiajie/Workspaces/server/test.dat");
        File base = new File("/home/tanjiajie/Workspaces2");
        System.out.println(getRelativePath(file, base));

    }

    public static void copyFilesToSdCard(Context ctx) {
        copyFileOrDir(ctx,""); // copy all files in assets folder in my project
    }

    private static void copyFileOrDir(Context ctx, String path) {
        AssetManager assetManager = ctx.getAssets();
        String assets[] = null;
        try {
            Log.i("tag", "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(ctx, path);
            } else {
                String fullPath =  getMapDirectory() + "/" + path;
                Log.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir( ctx,p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private static void copyFile(Context ctx, String filename) {
        AssetManager assetManager = ctx.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
//            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
//                newFileName = getMapDirectory() + "/" + filename.substring(0, filename.length()-4);
//            else
                newFileName = getMapDirectory() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of "+newFileName);
            Log.e("tag", "Exception in copyFile() "+e.toString());
        }

    }
}
