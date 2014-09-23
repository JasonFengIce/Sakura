package cn.ismartv.speedtester;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import cn.ismartv.speedtester.core.httpclient.NetWorkClient;
import cn.ismartv.speedtester.ui.fragment.FeedbackFragment;
import cn.ismartv.speedtester.ui.fragment.HelpFragment;
import cn.ismartv.speedtester.ui.fragment.NodeFragment;
import cn.ismartv.speedtester.utils.Utilities;
import com.crashlytics.android.Crashlytics;
import com.huaijie.tools.widget.viewpager_indicator.TabPageIndicator;


public class HomeActivity extends FragmentActivity {
    private static final String FILE_URL = "http://210.14.137.56/cdn/speedtest.ts";

    private static final String TAG = "HomeActivity";

    private static final Fragment[] FRAGMENTS = {new NodeFragment(), new FeedbackFragment(), new HelpFragment()};


    private FragmentPagerAdapter adapter;
    private String[] CONTENT;


    public static Handler messageHandler;

    public static final int NET_EXCEPTION = 0x0001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetWorkClient.getLatestAppVersion(this);

        Crashlytics.start(this);
        messageHandler = new MessageHandler();
        setContentView(R.layout.activity_home);
        CONTENT = new String[]{getResources().getString(R.string.node_list),
                getResources().getString(R.string.feedback),
                getResources().getString(R.string.help)};


        adapter = new HomeAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);


        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);

    }


    class HomeAdapter extends FragmentPagerAdapter {
        public HomeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return FRAGMENTS[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NET_EXCEPTION:
                    Utilities.showToast(getApplicationContext(), R.string.net_exception);
                    break;
                default:
                    break;
            }
        }
    }

}