package cn.ismartv.speedtester;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PointerView extends ImageView {

	public float mTotalChangeDegree = 0;
	
	public float mLastSpeed = 0;
	
	public float mCurrentSpeed = 0;
	
	public float mIncrement = 0;
	
	public float mDegree = 0; 
	
	public int duration = 200;
	
	private Bitmap mBitmap;
	
//	private Paint mPaint;
	
	private Matrix mMatrix;
	
	private boolean isStop = true;
	
//	private int mWidth;
//	private int mHeight;
//	private float mViewX;
//	private float mViewY;
//	
	private Handler mUpdateUIHandler = new Handler();
	private AnimatorTask mAnimatorTask;
	
	public PointerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PointerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PointerView(Context context) {
		super(context);
		init();
	}
	
	private void init(){
//		setDrawingCacheEnabled(true);
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dashboard_pointer);
//		mWidth = getMeasuredWidth();
//		mHeight = getMeasuredHeight();
//		mViewX = getX();
//		mViewY = getY();
//		mPaint = new Paint();
		mMatrix = new Matrix();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(mTotalChangeDegree==0){
//			mMatrix.setTranslate(175f, 55f);
//			mMatrix.postRotate(-132f, 5f, 125f);
			mMatrix.setRotate(-132, 5f, 125f);
			mMatrix.postTranslate(175, 55f);
			canvas.drawBitmap(mBitmap, mMatrix, null);
		} else {
			mMatrix.setRotate(-132+mTotalChangeDegree, 5f, 125f);
			mMatrix.postTranslate(175, 55f);
			canvas.drawBitmap(mBitmap, mMatrix, null);
		}
		
	}
	
	public void startHandle(){
		isStop =false;
		mAnimatorTask = new AnimatorTask();
		
		mUpdateUIHandler.post(mAnimatorTask);
	}
	
	public void stopHanlde(){
		mCurrentSpeed = 0;
		mIncrement = (mCurrentSpeed - mLastSpeed)/(float)(duration-100);
		float rate = mIncrement / 2000f;
		mDegree  = rate * 264;
		isStop =false;
	}
	
	public void updatePointer(float speed, int duration){
		mCurrentSpeed = speed;
		mIncrement = (mCurrentSpeed - mLastSpeed)/(float)(duration-100);
		float rate = mIncrement / 2000f;
		mDegree  = rate * 264;
	}
	
	class AnimatorTask implements Runnable {
		public void run() {
			if((mIncrement>0 && mLastSpeed<mCurrentSpeed) || (mIncrement<0 && mLastSpeed>mCurrentSpeed)){
				mLastSpeed += mIncrement;
				mTotalChangeDegree += mDegree;
				invalidate();
				mUpdateUIHandler.postDelayed(mAnimatorTask, 20);
			} else {
				if(isStop){
					mUpdateUIHandler.removeCallbacks(mAnimatorTask);
				} else {
					mUpdateUIHandler.postDelayed(mAnimatorTask, 100);
				}
			}
		}
		
	}
	
}
