package cn.edu.whu.indoorscene.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class CameraView extends TextureView {

	private int mRatioWidth = 0;
	private int mRatioHeight = 0;

	public CameraView(Context context) {
		this(context, null);
	}

	public CameraView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setAspectRatio(int width, int height) {
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("Size cannot be negative.");
		}
		mRatioWidth = width;
		mRatioHeight = height;
		requestLayout();
	}

	//LMY: Set the CameraView be square.
	public void setSquare(int width, int height) {
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("Size cannot be negative.");
		}
		if (width <= height) {
			mRatioWidth = width;
			mRatioHeight = width;
		} else {
			mRatioWidth = height;
			mRatioHeight = height;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		if (0 == mRatioWidth || 0 == mRatioHeight) {
			setMeasuredDimension(width, height);
//			setMeasuredDimension(width, width);
		} else {
			if (width < height * mRatioWidth / mRatioHeight) {
				setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//				setMeasuredDimension(width, width);
			} else {
				setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//				setMeasuredDimension(height, height);
			}
		}
	}

}