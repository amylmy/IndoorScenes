package cn.edu.whu.indoorscene.util;

import android.graphics.PointF;

/**
 * Created by tanjiajie on 2/12/17.
 */
public class GeometryUtils {

    public static float distance(final PointF p0, final PointF p1) {
        return new PointF(p0.x - p1.x, p0.y - p1.y).length();
    }
}
