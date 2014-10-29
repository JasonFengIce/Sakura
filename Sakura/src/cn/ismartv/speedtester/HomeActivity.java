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


public class HomeActivity extends Activity implements View.OnFocusChangeListener {
    private AnimationSet mAnimationSet;

    @InjectView(R.id.speed_tab)
    ImageView speedTab;

    @InjectView(R.id.feedback_tab)
    ImageView feedbackTab;

    @InjectView(R.id.help_tab)
    ImageView helpTab;

    FragmentTransaction transaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.transaction = getFragmentManager().beginTransaction();
        setContentView(R.layout.main);
        ButterKnife.inject(this);
        initViews();
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
            scaleAnimation.setDuration(500);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            view.startAnimation(animationSet);


        } else {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(2, 1f, 2, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(500);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            view.startAnimation(animationSet);
        }
    }


//    @OnClick({R.id.speed_tab, R.id.feedback_tab, R.id.help_tab})
//    public void changeTab(View view) {
//        Toast.makeText(this, "tset", Toast.LENGTH_LONG).show();
//
//        //增加点击放大效果
//        AnimationSet animationSet = new AnimationSet(true);
////        if (mAnimationSet != null && mAnimationSet != animationSet) {
////            ScaleAnimation scaleAnimation = new ScaleAnimation(2, 0.5f, 2, 0.5f,
////                    Animation.RELATIVE_TO_PARENT, 0.5f,   //使用动画播放图片
////                    Animation.RELATIVE_TO_PARENT, 0.5f);
////            scaleAnimation.setDuration(1000);
////            mAnimationSet.addAnimation(scaleAnimation);
////            mAnimationSet.setFillAfter(false); //让其保持动画结束时的状态。
////            view.startAnimation(mAnimationSet);
////        }
//        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 2f, 1, 2f,
//                Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        scaleAnimation.setDuration(500);
//        animationSet.addAnimation(scaleAnimation);
//        animationSet.setFillAfter(true);
//        view.startAnimation(animationSet);
//        mAnimationSet = animationSet;
//    }
}

