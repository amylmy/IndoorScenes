package cn.edu.whu.indoorscene.sensors;

/**
 * Created by caozp-ytcyc2007 on 2016/10/11.
 *
 */

public class MagneticValue {
    private double x;
    private double y;
    private double z;
    private double total;

    public MagneticValue(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.total = Math.sqrt(x*x + y*y + z*z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getTotal() {
        return total;
    }
}
