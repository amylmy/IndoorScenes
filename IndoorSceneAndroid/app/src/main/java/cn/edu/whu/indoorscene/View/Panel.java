package cn.edu.whu.indoorscene.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;

import cn.edu.whu.indoorscene.R;


/**
 * Created by Mengyun on 2016/10/26.
 *
 */

public class Panel extends SurfaceView implements SurfaceHolder.Callback {
    private TutorialThread mThread;
    private Context mContext;
    private GraphicObject graphicBackground=null;
    private GraphicObject graphic = null;

    // Drawing Tools
    Paint textPaint;
    Paint textOutlinePaint;
    Paint prebubblePaint;
    Paint bubblePaint;
    Paint bubbleShadowPaint;

    private static final String TAG = "Touch";

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Matrix _matrix = new Matrix();

    int WorkMode = -1; // idle, 0: training, 1: positioning

    // list of open info bubbles
	/* HashMap<Integer,Bubble> mBubbleMap = new HashMap <Integer,Bubble>() */
    ArrayList<Bubble> mBubbles=new ArrayList<Bubble>();
    ArrayList<Bubble> CurLineBubbles=new ArrayList<Bubble>();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int LOCATE = 3;
    int mode = NONE;
    float scale;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    public Panel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        getHolder().addCallback(this);
        setFocusable(true);
        mContext = context;
        initDrawingTools();
        //add background
        graphicBackground = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.cyc20));
        graphic = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.blauncher));
    }

    /**
     * setup the paint objects for drawing bubbles
     */
    private void initDrawingTools() {
        textPaint = new Paint();
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(30);
        textPaint.setTypeface(Typeface.SERIF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        textOutlinePaint = new Paint();
        textOutlinePaint.setColor(0xFF000000);
        textOutlinePaint.setTextSize(18);
        textOutlinePaint.setTypeface(Typeface.SERIF);
        textOutlinePaint.setTextAlign(Paint.Align.CENTER);
        textOutlinePaint.setStyle(Paint.Style.STROKE);
        textOutlinePaint.setStrokeWidth(2);

        prebubblePaint=new Paint();
        prebubblePaint.setColor(0xFFFF00FF);
        bubblePaint=new Paint();
        bubblePaint.setColor(0xFF00FF00);
        bubbleShadowPaint=new Paint();
        bubbleShadowPaint.setColor(0xFF00FF00);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (mThread.getSurfaceHolder()) {
            // Handle touch events here...
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    Log.d(TAG, "mode=DRAG");
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    Log.d(TAG, "oldDist=" + oldDist);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                        Log.d(TAG, "mode=ZOOM");
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = LOCATE;
                    Log.d(TAG, "mode=NONE");
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x,
                                event.getY() - start.y);
                    }
                    else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        Log.d(TAG, "newDist=" + newDist);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                    break;
            }
            return true;
        }
    }

    @SuppressWarnings("null")
    public void updateLocation(int x, int y) {
        x -= graphic.getGraphic().getWidth()/2;
        y -= graphic.getGraphic().getHeight()/2;

        if(x < 0)
            x = 0;
        if(y < 0)
            y = 0;
        graphic.setCoordinates(x,y);
    }

    public void addBubble(String text, int x, int y) {
        Bubble b = new Bubble(text, x, y);
        mBubbles.add(b);
    }

    public void setRPStatus(int Rp, int status) {
        if(mBubbles.size() > 0)
            mBubbles.get(mBubbles.size()-1).setStatus(status);
    }

    public void setWorkMode(int curMode) {
        WorkMode = curMode;
    }

    // for processing RP flag:
    /*
	 * Screen tapped x, y is screen coord from upper left and does not account
	 * for scroll
	 */
    void onScreenTapped(int x, int y, String str,int iLinePoint) {
        boolean missed = true;
        boolean bubble = false;
        // adjust for scroll
        int mScrollLeft = 0;
        int mScrollTop = 0;
        int mResizeFactorX = 1;
        int mResizeFactorY = 1;
        int testx = x-mScrollLeft;
        int testy = y-mScrollTop;

        // adjust for x y resize
        testx = (int)((float)testx/mResizeFactorX);
        testy = (int)((float)testy/mResizeFactorY);

        if (iLinePoint > 0)// be Line points
        {
            Bubble b = new Bubble(str, x, y);
            CurLineBubbles.add(iLinePoint-1, b);
        }
        else
        {
            // check if bubble tapped first
            // in case a bubble covers an area we want it to
            // have precedent
            Iterator<Bubble> it = mBubbles.iterator();
            while(it.hasNext())
            {
                Bubble b = it.next();
                if (b.isInArea((float)x-mScrollLeft,(float)y-mScrollTop)) {
                    //	b.onTapped();
                    bubble=true;
                    missed=false;
                    // only fire tapped for one bubble
                    break;
                }
            }

            if (!bubble) {
                // then add a new RP (Bubble)
                addBubble(str,testx, testy); // the second para TBD
            }
            else  //if (missed)
            {
                // here to operat on an existing bubble, TBD
                //	mBubbleMap.clear();
                invalidate();
            }
        }

    }

    @SuppressWarnings("null")
    public void AddRpFlag(PointF loc, String strRpId, int iLinePoint) {
        onScreenTapped((int)loc.x,(int)loc.y, strRpId, iLinePoint);
    }

    @SuppressWarnings("null")
    public PointF getMapCoor() {
        PointF locate = start;
        if (mode ==LOCATE)
        {
            mode = NONE;
            // Get the values of the matrix
            float[] values = new float[9];
            matrix.getValues(values);
            // event is the touch event for MotionEvent.ACTION_UP
            locate.x = (locate.x - values[2]) / values[0];
            locate.y = (locate.y - values[5]) / values[4];
        }
        else
        {
            //Note: Commenting this line may cause problem. LJB, 130415
            //	locate.x = -1;
        }
        return locate;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(canvas==null)
            return;
        canvas.drawColor(Color.WHITE);
        Bitmap bitmap;
        GraphicObject.Coordinates coords;
        //     Log.d(TAG, "mode=1");
        bitmap = graphicBackground.getGraphic();
        coords= graphicBackground.getCoordinates();
        canvas.drawBitmap(bitmap, matrix, new Paint());
        //  Log.d(TAG, "mode=2");
        if(WorkMode == 1){
            float[] matrixArray=new float[9];
            float[] matrixMobileArray=new float[9];
            matrix.getValues(matrixArray);

            float globalX = matrixArray[2];
            float globalY = matrixArray[5];
            matrixMobileArray= matrixArray;
            bitmap = graphic.getGraphic();
            coords = graphic.getCoordinates();
            matrix.getValues(matrixArray);
            _matrix.set(matrix);
            int a= (int) (graphic.getCoordinates().getX() * matrixArray[0]+globalX);
            int b= (int) (graphic.getCoordinates().getY() * matrixArray[4]+globalY);
            matrixMobileArray[2]=a;
            matrixMobileArray[5]=b;
            if(matrixMobileArray[0] > 0.5) {
                matrixMobileArray[2] += (matrixMobileArray[0] - 0.5)*graphic.getGraphic().getWidth()/2;
                matrixMobileArray[0]=(float) 0.5;
            }
            if(matrixMobileArray[4] > 0.5) {
                matrixMobileArray[5] += (matrixMobileArray[4] - 0.5)*graphic.getGraphic().getHeight()/2;
                matrixMobileArray[4]=(float) 0.5;
            }
            _matrix.setValues(matrixMobileArray);
            canvas.drawBitmap(bitmap, _matrix, new Paint());
        }
        if(WorkMode == 0) {
            drawBubbles(canvas,matrix);
        }
    }

    protected void drawBubbles(Canvas canvas, Matrix curMatrix) {
        float startX = -1;
        float startY = -1;
        float stopX = -1;
        float stopY = -1;
        int iCount=0;
        // first draw a line
        Iterator<Bubble> it = CurLineBubbles.iterator();
        while(it.hasNext())
        {
            Bubble b = it.next();
            if (iCount ==0)
            {
                startX=b.mX;
                startY=b.mY;
            }
            else
            {
                stopX=b.mX;
                stopY=b.mY;
            }
            iCount++;
        }

        if(startX >= 0 && stopX >= 0)
        {
            Paint LinePaint = new Paint();
            LinePaint.setColor(0xFF0000FF);
            LinePaint.setTextSize(30);
            LinePaint.setTypeface(Typeface.SERIF);
            LinePaint.setTextAlign(Paint.Align.CENTER);
            LinePaint.setAntiAlias(true);
            Path path= new Path();
            // draw pointer to origin
            path.moveTo(startX,startY);
            path.lineTo(startX+2, startY+2);
            path.lineTo(stopX+2, stopY+2);
            path.lineTo(stopX, stopY);
            path.lineTo(startX,startY);
            path.close();
            path.transform(curMatrix);
            canvas.drawPath(path, LinePaint);
        }

        // first draw bubbles
        Iterator<Bubble> itb = CurLineBubbles.iterator();
        while(itb.hasNext())
        {
            Bubble b = itb.next();
            b.onDraw(canvas, curMatrix);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new TutorialThread(getHolder(), this);
        mThread.setRunning(true);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // simply copied from sample application LunarLander:
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public class Bubble {
        String mText;
        int mStatus; //0, pre-ready, 1: done
        float mX;
        float mY;
        int mH;
        int mW;
        int mBaseline;
        float mTop;
        float mLeft;
        int yOffset=-24;

        Bubble(String text, float x, float y) {
            init(text,x,y);
        }

        void init(String text, float x, float y) {
            int mResizeFactorX = 1;
            int mResizeFactorY = 1;
            mStatus = 0;
            mText = text;
            mX = x*mResizeFactorX;
            mY = y*mResizeFactorY;
            Rect bounds = new Rect();
            textPaint.setTextScaleX(1.0f);
            textPaint.setColor(0xFFFFFF00);
            textPaint.getTextBounds(text, 0, mText.length(), bounds);
            mH = bounds.bottom - bounds.top + 20;
            mW = bounds.right - bounds.left + 20;

            int mViewWidth = getWidth();
            int mExpandWidth = graphicBackground.getGraphic().getWidth();
            if (mW > mViewWidth) {
                // too long for the display width...need to scale down
                float newscale = ((float) mViewWidth / (float) mW);
                textPaint.setTextScaleX(newscale);
                textPaint.getTextBounds(text, 0, mText.length(), bounds);
                mH = bounds.bottom - bounds.top + 20;
                mW = bounds.right - bounds.left + 20;
            }

            mBaseline = mH - bounds.bottom;
            mLeft = mX - (mW / 2);
            mTop = mY - mH + yOffset;

            // try to keep the bubble on screen
            if (mLeft < 0) {
                mLeft = 0;
            }
            if ((mLeft + mW) > mExpandWidth) {
                mLeft = mExpandWidth - mW;
            }
            if (mTop < 0) {
                mTop = mY -yOffset;
            }
        }

        public boolean isInArea(float x, float y) {
            boolean ret = false;
            if ((x>mLeft) && (x<(mLeft+mW))) {
                if ((y>mTop)&&(y<(mTop+mH))) {
                    ret = true;
                }
            }
            return ret;
        }

        public void setStatus(int status) {
            mStatus = status;
        }

        void onDraw(Canvas canvas, Matrix curMatrix) {

            // the shadow is NOT necessary
            // Draw a shadow of the bubble
            int mScrollLeft = 0;
            int mScrollTop = 0;
            float l = mLeft + mScrollLeft + 4;
            float t = mTop + mScrollTop + 4;
            Matrix Mat = new Matrix();
            Mat.set(curMatrix);
            //		canvas.drawRoundRect(new RectF(l,t,l+mW,t+mH), 20.0f, 20.0f, bubbleShadowPaint);
            Path path = new Path();
            float ox=mX+ mScrollLeft+ 1;
            float oy=mY+mScrollTop+ 1;

            if (mTop > mY) {
                yOffset=24;
            }
            // draw shadow of pointer to origin
            path.moveTo(ox,oy+20);
            path.lineTo(ox,oy+yOffset);
            path.lineTo(ox+4,oy+yOffset);
            path.lineTo(ox, oy+20);
            path.close();
            // path.
            path.transform(Mat);

            // draw the bubble
            l = mLeft + mScrollLeft;
            t = mTop + mScrollTop;

            float[] matrixArray=new float[9];
            Mat.getValues(matrixArray);
            if(matrixArray[0] < 1){
                matrixArray[0] = 1;
            }
            else{
                //matrixArray[0] = 1+ (matrixArray[0]-1)/2;
                l += (matrixArray[0] - 1)*mW/(2*matrixArray[0]);
            }
            if(matrixArray[4] < 1){
                matrixArray[4] = 1;
            }
            else{
                t += (matrixArray[4] - 1)*mH/(matrixArray[4]);
                t -= (matrixArray[4] - 1)*yOffset/(matrixArray[4]);
                //t += mH*(matrixArray[4]-1)/2;
            }

            if(mStatus != 0){
                t -= yOffset/(matrixArray[4]); //*(matrixArray[4]);
            }

            RectF rect = new RectF(l,t,l+mW/matrixArray[0],t+mH/matrixArray[4]);

            Mat.mapRect(rect, rect);
            if(mStatus == 0)
                canvas.drawRoundRect(rect, 10.0f, 10.0f, prebubblePaint);
            else
                canvas.drawRoundRect(rect, 10.0f, 10.0f, bubblePaint);

            Rect bounds = new Rect();
            textPaint.getTextBounds(mText, 0, mText.length(), bounds);

            canvas.drawText(mText, rect.centerX(), rect.centerY()+bounds.height()/2, textPaint);

            if(mStatus == 0)
            {
                path = new Path();
                ox=mX+ mScrollLeft;
                oy=mY+mScrollTop;
                yOffset=-24;
                if (mTop > mY) {
                    yOffset=24;
                }

                // draw pointer to origin
                path.moveTo(ox,oy);
                path.lineTo(ox+yOffset/(3*(matrixArray[0])),oy+yOffset/matrixArray[4]);
                path.lineTo(ox-yOffset/(3*(matrixArray[0])),oy+yOffset/matrixArray[4]);
                path.lineTo(ox, oy);
                path.close();
                path.transform(Mat);

                canvas.drawPath(path, prebubblePaint);
            }
        }
    }
}
