package cn.ismartv.speedtester.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ListView;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.ui.activity.HomeActivity;

/**
 * Created by huaijie on 1/8/15.
 */
public class SakuraListView extends ListView {
    private static final String TAG = "SakuraListView";
    private int tempPositioin = -1;
    private HomeActivityHoverBroadCastReceiver hoverBroadCastReceiver;

    public SakuraListView(Context context) {
        super(context);
        registerBroadCastReceiver(context);
    }

    public SakuraListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        registerBroadCastReceiver(context);
    }

    public SakuraListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        registerBroadCastReceiver(context);
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

    public void setMySelection(int position) {
        setFocusableInTouchMode(true);
        setFocusable(true);
        requestFocusFromTouch();
        requestFocus();
        setSelection(position);
    }


    public void setSelectionOne() {
        setFocusableInTouchMode(true);
        setFocusable(true);
        requestFocusFromTouch();
        requestFocus();
        setSelection(0);
    }

    private void registerBroadCastReceiver(Context context) {
        hoverBroadCastReceiver = new HomeActivityHoverBroadCastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HomeActivity.HOME_ACTIVITY_HOVER_ACTION);

        context.registerReceiver(hoverBroadCastReceiver, intentFilter);
    }

    class HomeActivityHoverBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HomeActivity.HOME_ACTIVITY_HOVER_ACTION)) {
                clearFocus();
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
