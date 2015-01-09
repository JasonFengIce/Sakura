package cn.ismartv.speedtester.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by huaijie on 1/9/15.
 */
public class SakuraButton extends Button {
    private static final String TAG = "SakuraButton";

    public SakuraButton(Context context) {
        super(context);
    }

    public SakuraButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SakuraButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE)) {
            setClickable(true);
            setFocusableInTouchMode(true);
            setFocusable(true);
            requestFocusFromTouch();
            requestFocus();

        } else {
            clearFocus();
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
