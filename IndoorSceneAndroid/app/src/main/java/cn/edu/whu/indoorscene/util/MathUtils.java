package cn.edu.whu.indoorscene.util;

import java.util.ArrayList;
import java.util.Vector;

public class MathUtils {


    public MathUtils(){}

    public static double[] covariance(ArrayList<Vector<Double>> gps,ArrayList<Vector<Double>> coor, double[] gps_mean, double[] coor_mean, int num_set) {

        double[] result = {0,0};

        for (int i = 0; i < num_set; i++) {
            result[0] += (((double)gps.get(i).get(0) - gps_mean[0]) * ((double)coor.get(i).get(0) - coor_mean[0]));
            result[1] += ((double)gps.get(i).get(1) - gps_mean[1]) * ((double)coor.get(i).get(1) - coor_mean[1]);
        }

        result[0] /= num_set - 1;
        result[1] /= num_set - 1;

        return result;
    }

    public static double[] variance(ArrayList<Vector<Double>> data, double[] mean, int size) {
        // Get the mean of the data set
        double[] sumOfSquaredDeviations = {0,0};

        // Loop through the data set
        for (int i = 0; i < size; i++) {
            // sum the difference between the data element and the mean squared
            sumOfSquaredDeviations[0] += Math.pow(((double)data.get(i).get(0) - mean[0]), 2);
            sumOfSquaredDeviations[1] += Math.pow(((double)data.get(i).get(1) - mean[1]), 2);

        }

                // Divide the sum by the length of the data set - 1 to get our result
        sumOfSquaredDeviations[0] /= (size - 1);
        sumOfSquaredDeviations[1] /= (size - 1);



        return sumOfSquaredDeviations;
    }


}
