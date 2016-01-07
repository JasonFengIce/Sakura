package cn.ismartv.iris.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import cn.ismartv.iris.R;
import cn.ismartv.iris.ui.fragment.FeedbackFragment;
import cn.ismartv.iris.ui.fragment.NodeFragment;
import cn.ismartv.viewpagerindicator.FragmentPager;
import cn.ismartv.viewpagerindicator.IconPagerIndicator;
import cn.ismartv.viewpagerindicator.IndicatorFragmentPagerAdapter;
import cn.ismartv.viewpagerindicator.RotationPagerTransformer;
import cn.ismartv.viewpagerindicator.ViewPagerScroller;

/**
 * Created by huaijie on 2015/4/7.
 */
public class HomeActivity extends FragmentActivity {
    private ArrayList<FragmentPager> fragments;

    private ViewPager viewPager;
    private IconPagerIndicator pagerIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_home);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerIndicator = (IconPagerIndicator) findViewById(R.id.indicator);

        fragments = new ArrayList<>();
        FragmentPager fragmentPager = new FragmentPager();
        fragmentPager.setFragment(new NodeFragment());
        fragmentPager.setIconResId(R.drawable.sakura_selector_indicator_node);
        fragments.add(fragmentPager);

        fragmentPager = new FragmentPager();
        fragmentPager.setFragment(new FeedbackFragment());
        fragmentPager.setIconResId(R.drawable.sakura_selector_indicator_feedback);
        fragments.add(fragmentPager);

        fragmentPager = new FragmentPager();
        fragmentPager.setFragment(new FeedbackFragment());
        fragmentPager.setIconResId(R.drawable.sakura_selector_indicator_help);
        fragments.add(fragmentPager);


        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.setScrollDuration(1500);
        scroller.initViewPagerScroll(viewPager);

        viewPager.setPageTransformer(false, new RotationPagerTransformer());

        viewPager.setAdapter(new IndicatorFragmentPagerAdapter(getSupportFragmentManager(), fragments));
        pagerIndicator.setViewPager(viewPager);

        pagerIndicator.setCurrentItem(position);
    }


}
