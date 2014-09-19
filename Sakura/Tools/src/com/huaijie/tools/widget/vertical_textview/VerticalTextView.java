package com.huaijie.tools.widget.vertical_textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import com.huaijie.tools.R;

/**
 * Created by <huaijiefeng@gmail.com> on 9/3/14.
 */
public class VerticalTextView extends View {

    private final static int DEFAULT_TEXT_SIZE = 15;
    private final static int DEFAULT_TEXT_COLOR = 0xFF000000;
    private int direction;


    private TextPaint mTextPaint;
    private String mText;
    private
    Rect textBounds = new Rect();


    public VerticalTextView(Context context) {
        super(context);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalTextView);
        CharSequence s = a.getString(R.styleable.VerticalTextView_text);

        if (s != null)
            mText = s.toString();

        int textSize = a.getDimensionPixelOffset(R.styleable.VerticalTextView_textSize, DEFAULT_TEXT_SIZE);

        if (textSize > 0)
            mTextPaint.setTextSize(textSize);

        mTextPaint.setColor(a.getColor(R.styleable.VerticalTextView_textColor, DEFAULT_TEXT_COLOR));
        direction = a.getInt(R.styleable.VerticalTextView_direction, 0);
        a.recycle();

        requestLayout();
        invalidate();
    }

    public VerticalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }


    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mTextPaint.getTextBounds(mText, 0, mText.length(), textBounds);
        setMeasuredDimension(
                measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = textBounds.height();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = textBounds.width();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int startX = 0;
        int startY = 0;
        int stopY = getHeight();
        Path path = new Path();
        if (direction == 0) {
            startX = (getWidth() >> 1) - (textBounds.height() >> 1);
            path.moveTo(startX, startY);
            path.lineTo(startX, stopY);
        } else {
            startX = (getWidth() >> 1) + (textBounds.height() >> 1);
            path.moveTo(startX, stopY);
            path.lineTo(startX, startY);
        }
        canvas.drawTextOnPath(mText, path, 0, 0, mTextPaint);
    }
}

