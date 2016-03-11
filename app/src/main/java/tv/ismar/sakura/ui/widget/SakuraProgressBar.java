package tv.ismar.sakura.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import tv.ismar.sakura.R;

/**
 * Created by fenghb on 14-7-28.
 */
public class SakuraProgressBar extends View {
    private static final String TAG = tv.ismar.sakura.ui.widget.SakuraProgressBar.class.getSimpleName();
    private float progress;

    int[] colors = {getColor(R.color.sakura_progress), getColor(R.color.sakura_progress)};
    float[] positioins = {getWidth() / 5, getWidth() * 2 / 5, getWidth() * 3 / 5, getWidth() * 4 / 5, getWidth()};

    Paint backgroundPaint = new Paint();
    Paint progressPaint = new Paint();
    LinearGradient linearGradient = new LinearGradient(0, 0, getWidth(), 0, colors, null, Shader.TileMode.MIRROR);

    Paint textPaint = new Paint();
    Paint dividePaint = new Paint();
    private RectF progressBarBackgroundRectF;
    private RectF progressBarRectF;


    public SakuraProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        backgroundPaint.setColor(Color.WHITE);


        progressPaint.setShader(linearGradient);


        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(15);
        textPaint.setTextAlign(Paint.Align.CENTER);

        dividePaint.setColor(Color.RED);
        dividePaint.setStrokeWidth(3);
        dividePaint.setAntiAlias(true);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        progressBarBackgroundRectF = new RectF(0, 0, getWidth(), getHeight());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        progressBarBackgroundRectF = new RectF(0, 0, getWidth(), getHeight());
        progressBarRectF = new RectF(0, 0, getWidth() / 100 * progress, getHeight());
        canvas.drawRect(progressBarBackgroundRectF, backgroundPaint);
        canvas.drawRect(progressBarRectF, progressPaint);
    }


    private int getColor(int res) {
        return getResources().getColor(res);
    }

    public void setProgress(final int progress) {
        this.progress = progress;
        invalidate();
    }

}
