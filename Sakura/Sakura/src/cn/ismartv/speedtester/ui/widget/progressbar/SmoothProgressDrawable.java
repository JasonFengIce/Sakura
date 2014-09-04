package cn.ismartv.speedtester.ui.widget.progressbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import cn.ismartv.speedtester.R;

/**
 * Created by castorflex on 11/10/13.
 */
public class SmoothProgressDrawable extends Drawable implements Animatable {

    public interface Callbacks {
        public void onStop();

        public void onStart();
    }

    private static final long FRAME_DURATION = 1000 / 60;
    private final static float OFFSET_PER_FRAME = 0.01f;

    private Callbacks mCallbacks;
    private Interpolator mInterpolator;
    private Rect mBounds;
    private Paint mPaint;
    private int[] mColors;
    private int mColorsIndex;
    private boolean mRunning;
    private float mCurrentOffset;
    private float mFinishingOffset;
    private int mSeparatorLength;
    private int mSectionsCount;
    private float mSpeed;
    private float mProgressiveStartSpeed;
    private float mProgressiveStopSpeed;
    private float mMaxOffset;
    private boolean mFinishing;
    private boolean mProgressiveStartActivated;
    private int mStartSection;
    private int mCurrentSections;
    private float mStrokeWidth;
    private Drawable mBackgroundDrawable;
    private boolean mUseGradients;
    private int[] mLinearGradientColors;
    private float[] mLinearGradientPositions;


    private SmoothProgressDrawable(Interpolator interpolator,
                                   int sectionsCount,
                                   int separatorLength,
                                   int[] colors,
                                   float strokeWidth,
                                   float speed,
                                   float progressiveStartSpeed,
                                   float progressiveStopSpeed,
                                   Callbacks callbacks,
                                   boolean progressiveStartActivated,
                                   Drawable backgroundDrawable,
                                   boolean useGradients) {
        mRunning = false;
        mInterpolator = interpolator;
        mSectionsCount = sectionsCount;
        mStartSection = 0;
        mCurrentSections = mSectionsCount;
        mSeparatorLength = separatorLength;
        mSpeed = speed;
        mProgressiveStartSpeed = progressiveStartSpeed;
        mProgressiveStopSpeed = progressiveStopSpeed;
        mColors = colors;
        mColorsIndex = 0;
        mFinishing = false;
        mBackgroundDrawable = backgroundDrawable;
        mStrokeWidth = strokeWidth;

        mMaxOffset = 1f / mSectionsCount;

        mPaint = new Paint();
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setDither(false);
        mPaint.setAntiAlias(false);

        mProgressiveStartActivated = progressiveStartActivated;
        mCallbacks = callbacks;

        mUseGradients = useGradients;
        refreshLinearGradientOptions();
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////         SETTERS
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) throw new IllegalArgumentException("Interpolator cannot be null");
        mInterpolator = interpolator;
        invalidateSelf();
    }

    public void setColors(int[] colors) {
        if (colors == null || colors.length == 0)
            throw new IllegalArgumentException("Colors cannot be null or empty");
        mColorsIndex = 0;
        mColors = colors;
        refreshLinearGradientOptions();
        invalidateSelf();
    }

    public void setColor(int color) {
        setColors(new int[]{color});
    }

    public void setSpeed(float speed) {
        if (speed < 0) throw new IllegalArgumentException("Speed must be >= 0");
        mSpeed = speed;
        invalidateSelf();
    }

    public void setProgressiveStartSpeed(float speed) {
        if (speed < 0) throw new IllegalArgumentException("SpeedProgressiveStart must be >= 0");
        mProgressiveStartSpeed = speed;
        invalidateSelf();
    }

    public void setProgressiveStopSpeed(float speed) {
        if (speed < 0) throw new IllegalArgumentException("SpeedProgressiveStop must be >= 0");
        mProgressiveStopSpeed = speed;
        invalidateSelf();
    }

    public void setSectionsCount(int sectionsCount) {
        if (sectionsCount <= 0) throw new IllegalArgumentException("SectionsCount must be > 0");
        mSectionsCount = sectionsCount;
        mMaxOffset = 1f / mSectionsCount;
        mCurrentOffset %= mMaxOffset;
        refreshLinearGradientOptions();
        invalidateSelf();
    }

    public void setSeparatorLength(int separatorLength) {
        if (separatorLength < 0)
            throw new IllegalArgumentException("SeparatorLength must be >= 0");
        mSeparatorLength = separatorLength;
        invalidateSelf();
    }

    public void setStrokeWidth(float strokeWidth) {
        if (strokeWidth < 0) throw new IllegalArgumentException("The strokeWidth must be >= 0");
        mPaint.setStrokeWidth(strokeWidth);
        invalidateSelf();
    }


    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (mBackgroundDrawable == backgroundDrawable) return;
        mBackgroundDrawable = backgroundDrawable;
        invalidateSelf();
    }

    public Drawable getBackgroundDrawable() {
        return mBackgroundDrawable;
    }

    public int[] getColors() {
        return mColors;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setProgressiveStartActivated(boolean progressiveStartActivated) {
        mProgressiveStartActivated = progressiveStartActivated;
    }

    public void setUseGradients(boolean useGradients) {
        if (mUseGradients == useGradients) return;

        mUseGradients = useGradients;
        refreshLinearGradientOptions();
        invalidateSelf();
    }

    protected void refreshLinearGradientOptions() {
        if (mUseGradients) {
            mLinearGradientColors = new int[mSectionsCount + 2];
            mLinearGradientPositions = new float[mSectionsCount + 2];
        } else {
            mPaint.setShader(null);
            mLinearGradientColors = null;
            mLinearGradientPositions = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////         DRAW

    @Override
    public void draw(Canvas canvas) {
        mBounds = getBounds();
        canvas.clipRect(mBounds);
        if (isRunning())
            drawStrokes(canvas);
        else {
            canvas.drawRect(100,200 , 900, 150, mPaint);
        }
    }


    private void drawStrokes(Canvas canvas) {

        float prevValue = 0f;
        int boundsWidth = mBounds.width();
        int width = boundsWidth + mSeparatorLength + mSectionsCount;
        int centerY = mBounds.centerY();
        float xSectionWidth = 1f / mSectionsCount;

        float startX;
        float endX;
        float prev;
        float end;
        float spaceLength;
        float xOffset;
        float ratioSectionWidth;
        float sectionWidth;
        float drawLength;
        int currentIndexColor = mColorsIndex;

        for (int i = 0; i <= mCurrentSections; ++i) {
            xOffset = xSectionWidth * i + mCurrentOffset;
            prev = Math.max(0f, xOffset - xSectionWidth);
            ratioSectionWidth = Math.abs(mInterpolator.getInterpolation(prev) -
                    mInterpolator.getInterpolation(Math.min(xOffset, 1f)));
            sectionWidth = (int) (width * ratioSectionWidth);

            if (sectionWidth + prev < width)
                spaceLength = Math.min(sectionWidth, mSeparatorLength);
            else
                spaceLength = 0f;

            drawLength = sectionWidth > spaceLength ? sectionWidth - spaceLength : 0;
            end = prevValue + drawLength;
            if (end > prevValue && i >= mStartSection) {
                float xFinishingOffset = mInterpolator.getInterpolation(Math.min(mFinishingOffset, 1f));
                startX = Math.max(xFinishingOffset * width, Math.min(boundsWidth, prevValue));
                endX = Math.min(boundsWidth, end);
                drawLine(canvas, boundsWidth, startX, centerY, endX, centerY, currentIndexColor);
            }


            prevValue = end + spaceLength;
            currentIndexColor = incrementColor(currentIndexColor);
        }

    }

    private void drawLine(Canvas canvas, int canvasWidth, float startX, float startY, float stopX, float stopY, int currentIndexColor) {
        mPaint.setColor(mColors[currentIndexColor]);

        canvas.drawLine(startX, startY, stopX, stopY, mPaint);
    }


    private int incrementColor(int colorIndex) {
        ++colorIndex;
        if (colorIndex >= mColors.length) colorIndex = 0;
        return colorIndex;
    }

    /**
     * Start the animation with the first color.
     * Calls progressiveStart(0)
     */
    public void progressiveStart() {
        progressiveStart(0);
    }

    /**
     * Start the animation from a given color.
     *
     * @param index
     */
    public void progressiveStart(int index) {
        resetProgressiveStart(index);
        start();
    }

    private void resetProgressiveStart(int index) {
        checkColorIndex(index);

        mCurrentOffset = 0;
        mFinishing = false;
        mFinishingOffset = 0f;
        mStartSection = 0;
        mCurrentSections = 0;
        mColorsIndex = index;
    }

    /**
     * Finish the animation by animating the remaining sections.
     */
    public void progressiveStop() {
        mFinishing = true;
        mStartSection = 0;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////         Animation: based on http://cyrilmottier.com/2012/11/27/actionbar-on-the-move/
    @Override
    public void start() {
        if (mProgressiveStartActivated) {
            resetProgressiveStart(0);
        }
        if (isRunning()) return;
        if (mCallbacks != null) {
            mCallbacks.onStart();
        }
        scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
        invalidateSelf();
    }

    @Override
    public void stop() {
        if (!isRunning()) return;
        if (mCallbacks != null) {
            mCallbacks.onStop();
        }
        mRunning = false;
        unscheduleSelf(mUpdater);
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        mRunning = true;
        super.scheduleSelf(what, when);
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    public boolean isStarting() {
        return mCurrentSections < mSectionsCount;
    }

    public boolean isFinishing() {
        return mFinishing;
    }

    private final Runnable mUpdater = new Runnable() {


        @Override
        public void run() {
            if (isFinishing()) {
                mFinishingOffset += (OFFSET_PER_FRAME * mProgressiveStopSpeed);
                mCurrentOffset += (OFFSET_PER_FRAME * mProgressiveStopSpeed);
                if (mFinishingOffset >= 1f) {
                    stop();
                }
            } else if (isStarting()) {
                mCurrentOffset += (OFFSET_PER_FRAME * mProgressiveStartSpeed);
            } else {
                mCurrentOffset += (OFFSET_PER_FRAME * mSpeed);
            }

            if (mCurrentOffset >= mMaxOffset) {
                mCurrentOffset -= mMaxOffset;
            }

            if (isRunning())
                scheduleSelf(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);

            invalidateSelf();
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////     Listener

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////     Checks

    private void checkColorIndex(int index) {
        if (index < 0 || index >= mColors.length) {
            throw new IllegalArgumentException(String.format("Index %d not valid", index));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////         BUILDER

    /**
     * Builder for SmoothProgressDrawable! You must use it!
     */
    public static class Builder {
        private Interpolator mInterpolator;
        private int mSectionsCount;
        private int[] mColors;
        private float mSpeed;
        private float mProgressiveStartSpeed;
        private float mProgressiveStopSpeed;
        private float mStrokeWidth;
        private int mStrokeSeparatorLength;
        private boolean mProgressiveStartActivated;
        private boolean mGenerateBackgroundUsingColors;
        private boolean mGradients;
        private Drawable mBackgroundDrawableWhenHidden;

        private Callbacks mOnProgressiveStopEndedListener;

        public Builder(Context context) {
            initValues(context);
        }

        public SmoothProgressDrawable build() {
            if (mGenerateBackgroundUsingColors) {
                mBackgroundDrawableWhenHidden = SmoothProgressBarUtils.generateDrawableWithColors(mColors, mStrokeWidth);
            }
            SmoothProgressDrawable ret = new SmoothProgressDrawable(
                    mInterpolator,
                    mSectionsCount,
                    mStrokeSeparatorLength,
                    mColors,
                    mStrokeWidth,
                    mSpeed,
                    mProgressiveStartSpeed,
                    mProgressiveStopSpeed,
                    mOnProgressiveStopEndedListener,
                    mProgressiveStartActivated,
                    mBackgroundDrawableWhenHidden,
                    mGradients);
            return ret;
        }

        private void initValues(Context context) {
            Resources res = context.getResources();
            mInterpolator = new AccelerateInterpolator();
            mSectionsCount = res.getInteger(R.integer.spb_default_sections_count);
            mColors = new int[]{res.getColor(R.color.spb_default_color)};
            mSpeed = Float.parseFloat(res.getString(R.string.spb_default_speed));
            mProgressiveStartSpeed = mSpeed;
            mProgressiveStopSpeed = mSpeed;
            mStrokeSeparatorLength = res.getDimensionPixelSize(R.dimen.spb_default_stroke_separator_length);
            mStrokeWidth = res.getDimensionPixelOffset(R.dimen.spb_default_stroke_width);
            mProgressiveStartActivated = res.getBoolean(R.bool.spb_default_progressiveStart_activated);
            mGradients = false;
        }

        public Builder interpolator(Interpolator interpolator) {
            if (interpolator == null)
                throw new IllegalArgumentException("Interpolator can't be null");
            mInterpolator = interpolator;
            return this;
        }

        public Builder sectionsCount(int sectionsCount) {
            if (sectionsCount <= 0) throw new IllegalArgumentException("SectionsCount must be > 0");
            mSectionsCount = sectionsCount;
            return this;
        }

        public Builder separatorLength(int separatorLength) {
            if (separatorLength < 0)
                throw new IllegalArgumentException("SeparatorLength must be >= 0");
            mStrokeSeparatorLength = separatorLength;
            return this;
        }

        public Builder color(int color) {
            mColors = new int[]{color};
            return this;
        }

        public Builder colors(int[] colors) {
            if (colors == null || colors.length == 0)
                throw new IllegalArgumentException("Your color array must not be empty");
            mColors = colors;
            return this;
        }

        public Builder strokeWidth(float width) {
            if (width < 0) throw new IllegalArgumentException("The width must be >= 0");
            mStrokeWidth = width;
            return this;
        }

        public Builder speed(float speed) {
            if (speed < 0) throw new IllegalArgumentException("Speed must be >= 0");
            mSpeed = speed;
            return this;
        }

        public Builder progressiveStartSpeed(float progressiveStartSpeed) {
            if (progressiveStartSpeed < 0)
                throw new IllegalArgumentException("progressiveStartSpeed must be >= 0");
            mProgressiveStartSpeed = progressiveStartSpeed;
            return this;
        }

        public Builder progressiveStopSpeed(float progressiveStopSpeed) {
            if (progressiveStopSpeed < 0)
                throw new IllegalArgumentException("progressiveStopSpeed must be >= 0");
            mProgressiveStopSpeed = progressiveStopSpeed;
            return this;
        }


        public Builder progressiveStart(boolean progressiveStartActivated) {
            mProgressiveStartActivated = progressiveStartActivated;
            return this;
        }

        public Builder callbacks(Callbacks onProgressiveStopEndedListener) {
            mOnProgressiveStopEndedListener = onProgressiveStopEndedListener;
            return this;
        }

        public Builder backgroundDrawable(Drawable backgroundDrawableWhenHidden) {
            mBackgroundDrawableWhenHidden = backgroundDrawableWhenHidden;
            return this;
        }

        public Builder generateBackgroundUsingColors() {
            mGenerateBackgroundUsingColors = true;
            return this;
        }

        public Builder gradients() {
            return gradients(true);
        }

        public Builder gradients(boolean useGradients) {
            mGradients = useGradients;
            return this;
        }
    }
}
