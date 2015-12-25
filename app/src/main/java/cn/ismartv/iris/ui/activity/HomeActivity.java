package cn.ismartv.iris.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import cn.ismartv.iris.R;
import cn.ismartv.iris.ui.adapter.IndicatorAdapter;
import cn.ismartv.iris.ui.fragment.FeedbackFragment;
import cn.ismartv.iris.ui.fragment.HelpFragment;
import cn.ismartv.iris.ui.fragment.NodeFragment;
import cn.ismartv.iris.ui.widget.indicator.IconPagerIndicator;
import cn.ismartv.iris.ui.widget.indicator.RotationPagerTransformer;
import cn.ismartv.iris.ui.widget.indicator.ViewPagerScroller;

import java.util.ArrayList;

/**
 * Created by huaijie on 2015/4/7.
 */
public class HomeActivity extends FragmentActivity {
    private IndicatorAdapter indicatorAdapter;
    private ArrayList<Fragment> fragments;

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

        fragments = new ArrayList<Fragment>();
        fragments.add(new NodeFragment());
        fragments.add(new FeedbackFragment());
        fragments.add(new HelpFragment());

        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.setScrollDuration(1500);
        scroller.initViewPagerScroll(viewPager);

        viewPager.setPageTransformer(false, new RotationPagerTransformer());
        indicatorAdapter = new IndicatorAdapter(getSupportFragmentManager(), fragments);

        viewPager.setAdapter(indicatorAdapter);
        pagerIndicator.setViewPager(viewPager);

        pagerIndicator.setCurrentItem(position);
    }


}
