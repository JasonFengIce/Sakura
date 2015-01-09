package cn.ismartv.speedtester.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


public class HomeActivity extends BaseActivity implements View.OnHoverListener {

    private static final String TAG = "HomeActivity";
    public boolean isFirstSpeedTest = true;
    @InjectView(R.id.indicator)
    IconPageIndicator indicator;
    @InjectView(R.id.pager)
    SakuraViewPager pager;
    private TabAdapter tabAdapter;

    private int position;


    private View mView;


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
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    /**
     * 返回键 监听器
     */
    private OnBackPressListener backPressListener;

    @Override
    public boolean onHover(View v, MotionEvent event) {
        activityHoverListener.onHover(v, event);
        Log.d("test", "adsff");
        return false;
    }

    public interface OnActivityHoverListener {
        public void onHover(View view, MotionEvent event);
    }

    private OnActivityHoverListener activityHoverListener;

    public void setActivityHoverListener(OnActivityHoverListener listener) {
        this.activityHoverListener = listener;
    }

    public interface OnBackPressListener {
        public void backPress();
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

    public interface OnKeyEventListener {
        public boolean onKeyDown(int keyCode, KeyEvent event);
    }

    private OnKeyEventListener onKeyEventListener;

    public void setOnKeyEventListener(OnKeyEventListener listener) {
        this.onKeyEventListener = listener;
    }
}

