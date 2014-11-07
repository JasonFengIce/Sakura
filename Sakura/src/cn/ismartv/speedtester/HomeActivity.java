package cn.ismartv.speedtester;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.download.DownloadTask;
import cn.ismartv.speedtester.ui.TabAdapter;
import cn.ismartv.speedtester.ui.fragment.FragmentSpeed;
import cn.ismartv.speedtester.ui.widget.indicator.IconPageIndicator;


public class HomeActivity extends FragmentActivity {
    private static final String TAG = "HomeActivity";

    private TabAdapter tabAdapter;

    @InjectView(R.id.indicator)
    IconPageIndicator indicator;

    @InjectView(R.id.pager)
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.inject(this);
        tabAdapter = new TabAdapter(getSupportFragmentManager());
        pager.setAdapter(tabAdapter);
        indicator.setViewPager(pager);

    }

    @Override
    protected void onDestroy() {

        CacheManager.updateLaunched(this, false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        FragmentSpeed fragmentSpeed = ((FragmentSpeed) tabAdapter.getSpeedFragment());
        DownloadTask downloadTask = fragmentSpeed.getDownloadTask();
        Log.d(TAG, "down " + downloadTask.isRunning());
        if (null != downloadTask && downloadTask.isRunning()) {
            downloadTask.setRunning(false);
        } else {
            if (fragmentSpeed.testProgressPopup.isShowing())
                fragmentSpeed.testProgressPopup.dismiss();
            super.onBackPressed();
        }
    }

    //
//    @InjectView(R.id.speed_tab)
//    ImageView speedTab;
//
//    @InjectView(R.id.feedback_tab)
//    ImageView feedbackTab;
//
//    @InjectView(R.id.help_tab)
//    ImageView helpTab;
//
//
//    private FragmentSpeed fragmentSpeed;
//    private FragmentFeedback fragmentFeedback;
//    private FragmentHelp fragmentHelp;
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Intent ootStartIntent = new Intent(this, HttpProxyService.class);
//        this.startService(ootStartIntent);
//
//        setContentView(R.layout.main);
//        ButterKnife.inject(this);
//        initViews();
//        fragmentSpeed = new FragmentSpeed();
//        fragmentFeedback = new FragmentFeedback();
//        fragmentHelp = new FragmentHelp();
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//        );
//        fragmentTransaction.replace(R.id.fragment, fragmentSpeed);
//        fragmentTransaction.commit();
//    }
//
//    public void initViews() {
//        speedTab.setOnFocusChangeListener(this);
//        feedbackTab.setOnFocusChangeListener(this);
//        helpTab.setOnFocusChangeListener(this);
//        helpTab.setOnClickListener(this);
//        speedTab.setOnClickListener(this);
//        feedbackTab.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//
//            case R.id.speed_tab:
//                FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
//                fragmentTransaction1.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                        R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//                );
//                fragmentTransaction1.replace(R.id.fragment, fragmentSpeed);
//                fragmentTransaction1.commit();
//
//                break;
//            case R.id.feedback_tab:
//
//                FragmentTransaction fragmentTransaction2 = getFragmentManager().beginTransaction();
//                fragmentTransaction2.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                        R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//                );
//                fragmentTransaction2.replace(R.id.fragment, fragmentFeedback);
//                fragmentTransaction2.commit();
//
//                break;
//            case R.id.help_tab:
//                FragmentTransaction fragmentTransaction3 = getFragmentManager().beginTransaction();
//                fragmentTransaction3.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                        R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//                );
//
//                fragmentTransaction3.replace(R.id.fragment, fragmentHelp);
//                fragmentTransaction3.commit();
//                break;
//            default:
//                break;
//        }
//
//    }
//
//    @Override
//    public void onFocusChange(View view, boolean focused) {
//        if (focused) {
//            AnimationSet animationSet = new AnimationSet(true);
//            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 2f, 1, 2f,
//                    Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            scaleAnimation.setDuration(200);
//            animationSet.addAnimation(scaleAnimation);
//            animationSet.setFillAfter(true);
//            view.startAnimation(animationSet);
//            switch (view.getId()) {
//
//                case R.id.speed_tab:
//                    FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
//                    fragmentTransaction1.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                            R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//                    );
//                    fragmentTransaction1.replace(R.id.fragment, fragmentSpeed);
//                    fragmentTransaction1.commit();
//
//                    break;
//                case R.id.feedback_tab:
//
//                    FragmentTransaction fragmentTransaction2 = getFragmentManager().beginTransaction();
//                    fragmentTransaction2.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                            R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//                    );
//                    fragmentTransaction2.replace(R.id.fragment, fragmentFeedback);
//                    fragmentTransaction2.commit();
//
//                    break;
//                case R.id.help_tab:
//                    FragmentTransaction fragmentTransaction3 = getFragmentManager().beginTransaction();
//                    fragmentTransaction3.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
//                            R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
//                    );
//
//                    fragmentTransaction3.replace(R.id.fragment, fragmentHelp);
//                    fragmentTransaction3.commit();
//                    break;
//                default:
//                    break;
//            }
//        } else {
//            AnimationSet animationSet = new AnimationSet(true);
//            ScaleAnimation scaleAnimation = new ScaleAnimation(2, 1f, 2, 1f,
//                    Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            scaleAnimation.setDuration(200);
//            animationSet.addAnimation(scaleAnimation);
//            animationSet.setFillAfter(true);
//            view.startAnimation(animationSet);
//        }
//    }


}

