package cn.ismartv.speedtester.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by huaijie on 1/9/15.
 */
public class SakuraTextView extends TextView {

    public SakuraTextView(Context context) {
        super(context);
    }

    public SakuraTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SakuraTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            setClickable(true);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            requestFocusFromTouch();
        } else {
            clearFocus();
        }
        return false;
    }
}
