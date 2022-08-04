package cn.edu.whu.indoorscene.sensors;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

/**
 * Created by caozp-ytcyc2007 on 2016/11/24.
 *
 */

public class MagneticValuesCaculator {
    List<MagneticValue> magValues;
    DescriptiveStatistics statsX;
    DescriptiveStatistics statsY;
    DescriptiveStatistics statsZ;
    DescriptiveStatistics statsTotal;
    MagneticValuesCaculator(List<MagneticValue> magValues) {
        this.magValues = magValues;
        this.statsX = new DescriptiveStatistics();
        this.statsY = new DescriptiveStatistics();
        this.statsZ = new DescriptiveStatistics();
        this.statsTotal = new DescriptiveStatistics();
        for( int i = 0; i < magValues.size(); i++) {
            statsX.addValue(magValues.get(i).getX());
            statsY.addValue(magValues.get(i).getY());
            statsZ.addValue(magValues.get(i).getZ());
            statsTotal.addValue(magValues.get(i).getTotal());
        }
    }

    double getXMean() {
        return this.statsX.getMean();
    }

    double getYMean() {
        return this.statsY.getMean();
    }

    double getZMean() {
        return this.statsZ.getMean();
    }

    double getTotalMean() {
        return this.statsTotal.getMean();
    }

    double getTotalMax() {
        return this.statsTotal.getMax();
    }

    double getTotalMin() {
        return this.statsTotal.getMin();
    }

    double getTotalGeometricMean() {
        return this.statsTotal.getGeometricMean();
    }

    double getTotalQuadraticMean() {
        return this.statsTotal.getQuadraticMean();
    }

    double getTotalStandardDeviation() {
        return this.statsTotal.getStandardDeviation();
    }

    double getTotalKurtosis() {
        return this.statsTotal.getKurtosis();
    }

    double getTotalSkewness() {
        return this.statsTotal.getSkewness();
    }

    double getTotalVariance() {
        return this.statsTotal.getVariance();
    }

    double getTotalPopulationVariance() {
        return this.statsTotal.getPopulationVariance();
    }
}
