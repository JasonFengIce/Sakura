package cn.ismartv.speedtester.ui.widget.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by huaijie on 8/6/14.
 */
public class SakuraViewPager extends ViewPager {

    public SakuraViewPager(Context context) {
        super(context);
    }

    public SakuraViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
            return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }


}
