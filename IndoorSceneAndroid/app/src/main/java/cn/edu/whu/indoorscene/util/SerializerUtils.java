package cn.edu.whu.indoorscene.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class SerializerUtils {
    public static void serialize(Serializable obj) throws IOException{

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                new File(FileUtils.getDataCollectionDirectory() ,"gps_calib_data.txt")  ));
        oos.writeObject(obj);
        System.out.println("Serialized" + obj.toString());
        oos.close();

    }
    public static GpsMappingUtils deserialize() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                new File(FileUtils.getDataCollectionDirectory() ,"gps_calib_data.txt")));
        GpsMappingUtils obj = null;
        try {
            obj = (GpsMappingUtils) ois.readObject();
        }catch(Exception e){};
        return obj;

    }
}
