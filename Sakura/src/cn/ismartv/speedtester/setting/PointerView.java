package cn.ismartv.speedtester.setting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import cn.ismartv.speedtester.R;

import java.util.HashSet;

public class PointerView extends ImageView {

	public float mTotalChangeDegree = 0;
	
	public float mLastSpeed = 0;
	
	public float mCurrentSpeed = 0;
	
	public float mIncrement = 0;
	
	public float mDegree = 0; 
	
	public int mDuration = 200;
	
	private Bitmap mBitmap;
	
//	private Paint mPaint;
	
	private Matrix mMatrix;
	
	private boolean isStop = true;
	
	/**
	 * Define a offset amount of the pointer's top-left point to the top-left point of the dashboard view
	 */
	private float mXOffset;
	private float mYOffset;
	
	/**
	 * Define a coordinate of the pointer's center point related to itself
	 */
	private float mCenterX;
	private float mCenterY;
	
	/**
	 * Define a max radius that pointer move between.
	 */
	private float mMaxDeflectionAngle;
	
//	private Context mContext;
	
	private Handler mUpdateUIHandler = new Handler();
	private AnimatorTask mAnimatorTask;
	
	private HashSet<OnAnimationStopListener> mListeners = new HashSet<OnAnimationStopListener>();
	
	public PointerView(Context context, AttributeSet attrs, int defStyle, float centerXOffset, float centerYOffset) {
		super(context, attrs, defStyle);
		mXOffset = centerXOffset;
		mYOffset = centerYOffset;
//		mContext = context;
		init();
	}

	public PointerView(Context context, AttributeSet attrs, float centerXOffset, float centerYOffset) {
		super(context, attrs);
		mXOffset = centerXOffset;
		mYOffset = centerYOffset;
//		mContext = context;
		init();
	}

	public PointerView(Context context, float centerXOffset, float centerYOffset) {
		super(context);
		mXOffset = centerXOffset;
		mYOffset = centerYOffset;
//		mContext = context;
		init();
	}
	
	private void init(){
//		setDrawingCacheEnabled(true);
//		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dashboard_pointer);
//		mViewX = getX();
//		mViewY = getY();
//		mPaint = new Paint();
		mMatrix = new Matrix();
	}

	public void setPointerImage(int resId, float centerX, float centerY) {
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dashboard_pointer);
		mCenterX = centerX;
		mCenterY = centerY;
	}
	
	public void setMaxDeflectionAngle(float angle){
		mMaxDeflectionAngle = angle;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(mTotalChangeDegree==0){
			mMatrix.setRotate(-mMaxDeflectionAngle, mCenterX, mCenterY);
			mMatrix.postTranslate(mXOffset, mYOffset);
			canvas.drawBitmap(mBitmap, mMatrix, null);
		} else {
			mMatrix.setRotate(-mMaxDeflectionAngle+mTotalChangeDegree, mCenterX, mCenterY);
			mMatrix.postTranslate(mXOffset, mYOffset);
			canvas.drawBitmap(mBitmap, mMatrix, null);
		}
		
	}
	
	public void startHandle(){
		isStop =false;
		mIncrement = 0;
		mDegree = 0;
		mCurrentSpeed = 0;
		mUpdateUIHandler.removeCallbacks(mAnimatorTask);
		mAnimatorTask = new AnimatorTask();
		
		mUpdateUIHandler.post(mAnimatorTask);
	}
	
	public void stopHanlde(){
		mCurrentSpeed = 0;
		mIncrement = (mCurrentSpeed - mLastSpeed)/((float)mDuration/20F);
		float rate = mIncrement / 2000f;
		mDegree  = rate * mMaxDeflectionAngle * 2f;
		isStop =true;
	}
	
	public void updatePointer(float speed, int duration){
		mCurrentSpeed = speed>=2000f ? 2000f : speed;
		mIncrement = (mCurrentSpeed - mLastSpeed)/((float)duration/20F);
		float rate = mIncrement / 2000f;
		mDegree  = rate * mMaxDeflectionAngle * 2f;
	}
	
	class AnimatorTask implements Runnable {
		public void run() {
			if((mIncrement>0 && mLastSpeed<mCurrentSpeed) || (mIncrement<0 && (!isStop) && mLastSpeed>mCurrentSpeed) || (mIncrement<0 && isStop && mTotalChangeDegree>0)){
				mLastSpeed += mIncrement;
				mTotalChangeDegree += mDegree;
				mTotalChangeDegree = mTotalChangeDegree<0 ? 0 : mTotalChangeDegree;
				invalidate();
				if(isStop && !mListeners.isEmpty()){
					float currentShowSpeed = mLastSpeed<0 ? 0: mLastSpeed;
					for(OnAnimationStopListener listener:mListeners){
						listener.onSpeedChange(currentShowSpeed);
					}
				}
				mUpdateUIHandler.postDelayed(mAnimatorTask, 20);
			} else {
				if(isStop){
					mTotalChangeDegree = 0;
					mLastSpeed = 0;
					mUpdateUIHandler.removeCallbacks(mAnimatorTask);
					if(!mListeners.isEmpty()){
						for(OnAnimationStopListener listener:mListeners){
							listener.onStop();
						}
					}
				} else {
					mUpdateUIHandler.postDelayed(mAnimatorTask, 100);
				}
			}
		}
		
	}
	
	public float getCenterXOffset(){
		return mCenterX+mXOffset;
	}
	public float getCenterYOffset(){
		return mCenterY+mYOffset;
	}
	
	public static interface OnAnimationStopListener {
		public void onStop();
		public void onSpeedChange(float currentSpeed);
	}
	
	public void setOnAnimationStopListener(OnAnimationStopListener listener){
		mListeners.add(listener);
	}
	
	public boolean hasStopped(){
		return isStop && (mLastSpeed==0);
	}
}
