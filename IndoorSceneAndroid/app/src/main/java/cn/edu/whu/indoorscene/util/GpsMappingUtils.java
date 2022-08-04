package cn.edu.whu.indoorscene.util;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;


public class GpsMappingUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    private static ArrayList<Vector<Double>> calib_gps = new ArrayList<Vector<Double>>();
    private static ArrayList<Vector<Double>> calib_coor = new ArrayList<Vector<Double>>();
    private static double[] coor_mean = {0,0};
    private static double[] gps_mean = {0,0};
    private static double ax;
    private static double bx;
    private static double ay;
    private static double by;
    private static boolean updated = false;

    private static int num_set = 0;

    private static void calibrate(){
        double covar[] = MathUtils.covariance(calib_gps, calib_coor, gps_mean, coor_mean, num_set);



        double vari[]  = MathUtils.variance(calib_gps, gps_mean, num_set);

        bx = covar[0]/vari[0] ;
        ax = coor_mean[0] - bx * gps_mean[0];

        by = covar[1]/vari[1] ;
        ay = coor_mean[1] - by * gps_mean[1];
        updated = true;
    }



    public static double[] compute(double longi, double lati){
        double[] result = {0.0,0.0};
        if(num_set >= 3){
            if(!updated)
                calibrate();

            result[0] = ax + bx*longi;
            result[1] = ay + by*lati;

            result[0] = Math.floor(result[0]*1000)/1000;
            result[1] = Math.floor(result[1]*1000)/1000;
        }

        return result;
    }

    public static void addCalibData(Location location, Vector<Double> coor){

        Vector<Double> temp = new Vector<Double>(2);

        temp.add(location.getLongitude());
        temp.add(location.getLatitude());

        calib_gps.add(temp);
        calib_coor.add(coor);



        gps_mean[0] = (gps_mean[0]*num_set+(double)temp.get(0))/(num_set+1);
        gps_mean[1] = (gps_mean[1]*num_set+(double)temp.get(1))/(num_set+1);
        coor_mean[0] = (coor_mean[0]*num_set+(double)coor.get(0))/(num_set+1);
        coor_mean[1] = (coor_mean[1]*num_set+(double)coor.get(1))/(num_set+1);

        ++num_set;
        updated = false;
//        printCalibData();
    }

    public static void printCalibData(){
        for (int i =0; i< num_set; ++i)
            System.out.println(
                    "Calib Data " + i + " " +
                    calib_gps.get(i).get(0) + " " + calib_gps.get(i).get(1) + " " +
                    calib_coor.get(i).get(0) + " " + calib_coor.get(i).get(1) + " "

            );

    }


    public static void addCalibData(double longi,double lati, double coor_x, double coor_y){
        try {
        Vector<Double> temp = new Vector<Double>(2);
        temp.add(longi);
        temp.add(lati);

        calib_gps.add(temp);


        temp = new Vector<Double>(2);
        temp.add(coor_x);
        temp.add(coor_y);
        calib_coor.add(temp);

        gps_mean[0] = (gps_mean[0]*num_set+longi)/(num_set+1);
        gps_mean[1] = (gps_mean[1]*num_set+lati)/(num_set+1);



        coor_mean[0] = (coor_mean[0]*num_set+coor_x)/(num_set+1);
        coor_mean[1] = (coor_mean[1]*num_set+coor_y)/(num_set+1);

        ++num_set;
        updated = false;
        }catch(Exception e){ e.printStackTrace();};
    }

    
    @Override
    public String toString(){
        calibrate();
        String temp = new String("");
        temp += "Calibration Data Set\n";
        for (int i =0; i< num_set; ++i)
             temp += "Calib Data " + i + " " + calib_gps.get(i).get(0) + " " + calib_gps.get(i).get(1) + " " +
                            calib_coor.get(i).get(0) + " " + calib_coor.get(i).get(1) + "\n";



        temp += "\n";
        temp += "ax: " + ax;
        temp += "bx: " + bx + "\n";
        temp += "ay: " + ay;
        temp += "by: " + by + "\n";
        temp += "Num of set: " + num_set;

        return temp;
    }

}
