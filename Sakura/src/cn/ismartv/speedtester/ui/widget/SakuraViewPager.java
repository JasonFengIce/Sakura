package cn.ismartv.speedtester.ui.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by huaijie on 1/8/15.
 */
public class SakuraViewPager extends ViewPager {
    public SakuraViewPager(Context context) {
        super(context);
    }

    public SakuraViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = true;
                    break;
                case KeyEvent.KEYCODE_TAB:
                    if (Build.VERSION.SDK_INT >= 11) {
                        // The focus finder had a bug handling FOCUS_FORWARD and FOCUS_BACKWARD
                        // before Android 3.0. Ignore the tab key on those devices.
                        handled = true;
                    }
                    break;
            }
        }
        return handled;
    }
}
