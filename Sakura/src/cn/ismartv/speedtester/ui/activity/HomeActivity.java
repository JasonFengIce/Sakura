package cn.ismartv.speedtester.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.ui.TabAdapter;
import cn.ismartv.speedtester.ui.widget.SakuraViewPager;
import cn.ismartv.speedtester.ui.widget.indicator.IconPageIndicator;
import cn.ismartv.speedtester.ui.widget.indicator.RotationPagerTransformer;
import cn.ismartv.speedtester.ui.widget.indicator.ViewPagerScroller;


public class HomeActivity extends BaseActivity implements View.OnHoverListener {

    public static final String KEYCODE_DPAD_LEFT = "KEYCODE_DPAD_LEFT";
    public static final String KEYCODE_DPAD_RIGHT = "KEYCODE_DPAD_RIGHT";
    public static final String HOME_ACTIVITY_HOVER_ACTION = "HOME_ACTIVITY_HOVER_ACTION";
    private static final String TAG = "HomeActivity";
    public boolean isFirstSpeedTest = true;
    @InjectView(R.id.indicator)
    IconPageIndicator indicator;
    @InjectView(R.id.pager)
    SakuraViewPager pager;
    private KeyCodeBroadCastReceiver keyCodeBroadCastReceiver;
    private TabAdapter tabAdapter;

    private int position;


    private View mView;
    /**
     * 返回键 监听器
     */
    private OnBackPressListener backPressListener;
    private OnKeyEventListener onKeyEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////////////////////////////////////////////
        //Get The Position Of Page
        /////////////////////////////////////////////////////////////
        Intent intent = getIntent();
        position = intent.getIntExtra(MenuActivity.TAB_FLAG, 0);

        mView = LayoutInflater.from(this).inflate(R.layout.main, null);
        mView.setOnHoverListener(this);
        setContentView(mView);
        ButterKnife.inject(this);

        tabAdapter = new TabAdapter(getSupportFragmentManager());

        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.setScrollDuration(1500);
        scroller.initViewPagerScroll(pager);//这个是设置切换过渡时间为2秒

        pager.setAdapter(tabAdapter);

        pager.setPageTransformer(false, new RotationPagerTransformer());



        indicator.setViewPager(pager);

        /////////////////////////////////////////////////////////////
        //Init Page Position
        /////////////////////////////////////////////////////////////
        pager.setCurrentItem(position);

    }

    @Override
    protected void onResume() {
        super.onResume();
        keyCodeBroadCastReceiver = new KeyCodeBroadCastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(KEYCODE_DPAD_LEFT);
        intentFilter.addAction(KEYCODE_DPAD_RIGHT);

        registerReceiver(keyCodeBroadCastReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(keyCodeBroadCastReceiver);
    }

    @Override
    protected void onDestroy() {

        CacheManager.updateLaunched(this, false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        Intent intent = new Intent();
        intent.setAction(HOME_ACTIVITY_HOVER_ACTION);
        sendBroadcast(intent);

        return false;
    }

    public void setBackPressListener(OnBackPressListener listener) {
        this.backPressListener = listener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                return onKeyEventListener.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    public void setOnKeyEventListener(OnKeyEventListener listener) {
        this.onKeyEventListener = listener;
    }

    public interface OnBackPressListener {
        public void backPress();
    }

    public interface OnKeyEventListener {
        public boolean onKeyDown(int keyCode, KeyEvent event);
    }

    class KeyCodeBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(KEYCODE_DPAD_LEFT))
                pager.setCurrentItem(0);
            else if (intent.getAction().equals(KEYCODE_DPAD_RIGHT))
                pager.setCurrentItem(2);
        }
    }
}

