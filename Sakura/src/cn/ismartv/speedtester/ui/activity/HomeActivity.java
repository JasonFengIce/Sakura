package cn.ismartv.speedtester.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.ui.TabAdapter;
import cn.ismartv.speedtester.ui.widget.indicator.IconPageIndicator;


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
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    /**
     * 返回键 监听器
     */
    private OnBackPressListener backPressListener;

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

