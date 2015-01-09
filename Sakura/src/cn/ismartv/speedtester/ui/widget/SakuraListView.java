package cn.ismartv.speedtester.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;
import cn.ismartv.speedtester.AppConstant;

/**
 * Created by huaijie on 1/8/15.
 */
public class SakuraListView extends ListView {
    private static final String TAG = "SakuraListView";

    public SakuraListView(Context context) {
        super(context);
    }

    public SakuraListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SakuraListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        int position = pointToPosition((int) event.getX(), (int) event.getY());
        if (AppConstant.DEBUG) {

            Log.d(TAG, "list position is --->" + position);
            Log.d(TAG, "hover event is --->" + event.getAction());
        }
        if ((event.getAction() == MotionEvent.ACTION_HOVER_ENTER && position != -1) || (event.getAction() == MotionEvent.ACTION_HOVER_MOVE && position != -1)) {

            setFocusableInTouchMode(true);
            setFocusable(true);
            requestFocusFromTouch();
            requestFocus();
            setSelection(position);
        } else {
            clearFocus();
        }
        return true;
    }

    public void dispatchHoverEvent(MotionEvent event, boolean clearFocus) {
        if (clearFocus) {
            clearFocus();
        } else {
            dispatchHoverEvent(event);
        }
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return super.onInterceptHoverEvent(event);
    }
}
