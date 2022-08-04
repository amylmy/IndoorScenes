package cn.edu.whu.indoorscene.View;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Mengyun on 2016/10/26.
 */

public class TutorialThread extends Thread {
    private SurfaceHolder mSurfaceHolder;
    private Panel mPanel;
    private boolean mRun = false;

    public TutorialThread(SurfaceHolder surfaceHolder, Panel panel) {
        mSurfaceHolder = surfaceHolder;
        mPanel = panel;
    }

    public void setRunning(Boolean run) {
        mRun = run;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    public void run() {
        Canvas c;
        while (mRun) {
            c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    //mPanel.updatePhysics();
                    mPanel.onDraw(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // we will try it again and again...
            }
            finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }

    }

}
