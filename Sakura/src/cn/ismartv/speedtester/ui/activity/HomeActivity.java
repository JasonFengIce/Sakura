package cn.ismartv.speedtester.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.DownloadTask;
import cn.ismartv.speedtester.ui.TabAdapter;
import cn.ismartv.speedtester.ui.fragment.FragmentSpeed;
import cn.ismartv.speedtester.ui.widget.indicator.IconPageIndicator;
import com.ismartv.android.vod.core.install.BootInstallTask;
import com.ismartv.android.vod.service.HttpProxyService;


public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";
    public boolean isFirstSpeedTest = true;
    @InjectView(R.id.indicator)
    IconPageIndicator indicator;
    @InjectView(R.id.pager)
    ViewPager pager;
    private TabAdapter tabAdapter;

    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////////////////////////////////////////////
        //Get The Position Of Page
        /////////////////////////////////////////////////////////////
        Intent intent = getIntent();
        position = intent.getIntExtra(MenuActivity.TAB_FLAG, 0);
        setContentView(R.layout.main);
        ButterKnife.inject(this);
        tabAdapter = new TabAdapter(getSupportFragmentManager());
        pager.setAdapter(tabAdapter);
        indicator.setViewPager(pager);
        /////////////////////////////////////////////////////////////
        //Init Page Position
        /////////////////////////////////////////////////////////////
        pager.setCurrentItem(position);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        CacheManager.updateLaunched(this, false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FragmentSpeed fragmentSpeed = (FragmentSpeed) tabAdapter.getSpeedFragment();
        if (null != fragmentSpeed) {
            if (!fragmentSpeed.getTaskStatusIsCancelled()) {
                super.onBackPressed();
            }else {
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }


    }


}

