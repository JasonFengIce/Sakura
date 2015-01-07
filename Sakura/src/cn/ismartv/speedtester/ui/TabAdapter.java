package cn.ismartv.speedtester.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ImageView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.ui.fragment.FragmentFeedback;
import cn.ismartv.speedtester.ui.fragment.FragmentHelp;
import cn.ismartv.speedtester.ui.fragment.FragmentSpeed;
import cn.ismartv.speedtester.ui.widget.indicator.IconPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaijie on 14-10-29.
 */
public class TabAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

    protected static final int[] ICONS = new int[]{
            R.drawable.selector_tab_speed,
            R.drawable.selector_tab_help,
            R.drawable.selector_tab_feedback
    };

    FragmentSpeed fragmentSpeed;

    private int mCount = ICONS.length;
    private Fragment[] FRAGMENTS = new Fragment[3];


    public TabAdapter(FragmentManager fm) {
        super(fm);




        fragmentSpeed = new FragmentSpeed();
        FRAGMENTS[0] = fragmentSpeed;
        FRAGMENTS[1] = new FragmentFeedback();
        FRAGMENTS[2] = new FragmentHelp();

    }

    public Fragment getSpeedFragment() {
        return getItem(0);
    }

    @Override
    public Fragment getItem(int position) {
        return FRAGMENTS[position];
    }

    @Override
    public int getIconResId(int index) {
        return ICONS[index % ICONS.length];
    }

    @Override
    public int getCount() {
        return mCount;
    }
}
