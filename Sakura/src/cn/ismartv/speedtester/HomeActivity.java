package cn.ismartv.speedtester;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.ui.fragment.FragmentFeedback;
import cn.ismartv.speedtester.ui.fragment.FragmentHelp;
import cn.ismartv.speedtester.ui.fragment.FragmentSpeed;


public class HomeActivity extends Activity implements View.OnFocusChangeListener {
    private static final String TAG = "HomeActivity";

    @InjectView(R.id.speed_tab)
    ImageView speedTab;

    @InjectView(R.id.feedback_tab)
    ImageView feedbackTab;

    @InjectView(R.id.help_tab)
    ImageView helpTab;


    private FragmentSpeed fragmentSpeed;
    private FragmentFeedback fragmentFeedback;
    private FragmentHelp fragmentHelp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.inject(this);
        initViews();
        fragmentSpeed = new FragmentSpeed();
        fragmentFeedback = new FragmentFeedback();
        fragmentHelp = new FragmentHelp();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
                R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
        );
        fragmentTransaction.replace(R.id.fragment, fragmentSpeed);
        fragmentTransaction.commit();
    }

    public void initViews() {
        speedTab.setOnFocusChangeListener(this);
        feedbackTab.setOnFocusChangeListener(this);
        helpTab.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean focused) {
        if (focused) {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 2f, 1, 2f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(200);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            view.startAnimation(animationSet);
            switch (view.getId()) {

                case R.id.speed_tab:
                    FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
                    fragmentTransaction1.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
                            R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
                    );
                    fragmentTransaction1.replace(R.id.fragment, fragmentSpeed);
                    fragmentTransaction1.commit();

                    break;
                case R.id.feedback_tab:

                    FragmentTransaction fragmentTransaction2 = getFragmentManager().beginTransaction();
                    fragmentTransaction2.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
                            R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
                    );
                    fragmentTransaction2.replace(R.id.fragment, fragmentFeedback);
                    fragmentTransaction2.commit();

                    break;
                case R.id.help_tab:
                    FragmentTransaction fragmentTransaction3 = getFragmentManager().beginTransaction();
                    fragmentTransaction3.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
                            R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit
                    );
                    fragmentTransaction3.replace(R.id.fragment, fragmentHelp);
                    fragmentTransaction3.commit();
                    break;
                default:
                    break;
            }
        } else {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(2, 1f, 2, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(200);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            view.startAnimation(animationSet);
        }
    }
}

