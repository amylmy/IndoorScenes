package cn.edu.whu.indoorscene.Data;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mengyun on 2016/12/14.
 *
 */

public class Location extends PointF implements Parcelable{
    public final static Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            float x = source.readFloat();
            float y = source.readFloat();
            String floor = (String) source.readValue(String.class.getClassLoader());
            return new Location(x, y, floor);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    private String floor;

    public Location(float x, float y, String floor){
        super(x, y);
        this.floor = floor;
    }

    public Location(PointF location2D, String floor) {
        if (location2D == null) {
            this.x = Float.NaN;
            this.y = Float.NaN;
        } else {
            this.x = location2D.x;
            this.y = location2D.y;
        }
        this.floor = floor;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeValue(floor);
    }

}
