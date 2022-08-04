package cn.edu.whu.indoorscene.View;

import android.graphics.Bitmap;

/**
 * Created by Mengyun on 2016/10/26.
 */

public class GraphicObject {
    private Bitmap mBitmap;
    private Coordinates mCoordinates;
    private Speed mSpeed;

    public GraphicObject (Bitmap bitmap) {
        mBitmap = bitmap;
        mCoordinates = new Coordinates();
        mSpeed = new Speed();
    }

    public void setCoordinates(int x, int y) {
        mCoordinates.x = x;
        mCoordinates.y = y;
    }

    public Bitmap getGraphic() {
        return mBitmap;
    }

    public Speed getSpeed() {
        return mSpeed;
    }

    public Coordinates getCoordinates() {
        return mCoordinates;
    }

    /**
     * Contains the coordinates of the graphic.
     */
    public class Coordinates {
        private int x = 0;
        private int y = 0;

        public void setX(int value) {
            x = value;
        }
        public void setY(int value){
            y = value;
        }

        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }

        public String toString(){
            return "Coordinates: (" + x + "," + y + ")";
        }
    }

    public class Speed {
        public static final int X_DIRECTION_RIGHT = 1;
        public static final int X_DIRECTION_LEFT = -1;
        public static final int Y_DIRECTION_DOWN = 1;
        public static final int Y_DIRECTION_UP = -1;

        private int x = 1;
        private int y = 1;

        private int xDirection = X_DIRECTION_RIGHT;
        private int yDirection = Y_DIRECTION_DOWN;

        public String toString() {
            String xDirectionToString;
            String yDirectionToString;
            if (xDirection == X_DIRECTION_RIGHT) {
                xDirectionToString = "right";
            } else {
                xDirectionToString = "left";
            }
            if (yDirection == Y_DIRECTION_UP) {
                yDirectionToString = "up";
            } else {
                yDirectionToString = "down";
            }
            return "Speed: x: " + x + " | y: " + y + " | xDirection: " + xDirectionToString + " | yDirection: " + yDirectionToString;
        }

    }

}
