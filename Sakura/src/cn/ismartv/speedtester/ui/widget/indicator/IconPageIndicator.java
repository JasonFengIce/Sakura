/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ismartv.speedtester.ui.widget.indicator;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import cn.ismartv.speedtester.R;

import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class IconPageIndicator extends HorizontalScrollView implements PageIndicator, View.OnClickListener {
    private final IcsLinearLayout mIconsLayout;

    private ViewPager mViewPager;
    private OnPageChangeListener mListener;
    private Runnable mIconSelector;
    private int mSelectedIndex;

    public IconPageIndicator(Context context) {
        this(context, null);
    }

    public IconPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mIconsLayout = new IcsLinearLayout(context, R.attr.vpiIconPageIndicatorStyle);
        addView(mIconsLayout, new LayoutParams(WRAP_CONTENT, FILL_PARENT, Gravity.CENTER));
    }

    private void animateToIcon(final int position) {
        final View iconView = mIconsLayout.getChildAt(position);
        if (mIconSelector != null) {
            removeCallbacks(mIconSelector);
        }
        mIconSelector = new Runnable() {
            public void run() {
                final int scrollPos = iconView.getLeft() - (getWidth() - iconView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mIconSelector = null;
            }
        };
        post(mIconSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIconSelector != null) {
            // Re-post the selector we saved
            post(mIconSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mIconSelector != null) {
            removeCallbacks(mIconSelector);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (mListener != null) {
            mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        if (mListener != null) {
            mListener.onPageSelected(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mIconsLayout.removeAllViews();
        IconPagerAdapter iconAdapter = (IconPagerAdapter) mViewPager.getAdapter();
        int count = iconAdapter.getCount();
        View linearLayout = LayoutInflater.from(getContext()).inflate(R.layout.indicator_icon, null);
        ImageView imageView1 = (ImageView) linearLayout.findViewById(R.id.icon_node);
        ImageView imageView2 = (ImageView) linearLayout.findViewById(R.id.icon_help);
        ImageView imageView3 = (ImageView) linearLayout.findViewById(R.id.icon_feedback);

        ImageView[] imageViews = {imageView1, imageView2, imageView3};

        for (int i = 0; i < count; i++) {
            imageViews[i].setImageResource(iconAdapter.getIconResId(i));
            imageViews[i].setOnClickListener(this);

//
//
//            ImageView view = new ImageView(getContext(), null, R.attr.vpiIconPageIndicatorStyle);
//            view.setImageResource(iconAdapter.getIconResId(i));
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
//            layoutParams.setMargins(80, 0, 80, 0);
//
//            view.setLayoutParams(layoutParams);
//            mIconsLayout.addView(view);
        }
        mIconsLayout.addView(linearLayout);

        if (mSelectedIndex > count) {
            mSelectedIndex = count - 1;
        }
        setCurrentItem(mSelectedIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mSelectedIndex = item;
        mViewPager.setCurrentItem(item);

        int tabCount = mIconsLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            View child = mIconsLayout.getChildAt(i);
            boolean isSelected = (i == item);
            child.setSelected(isSelected);
            if (isSelected) {
                animateToIcon(item);
            }
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 2f, 1, 2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        if (null != pretTab && view.getId() != pretTab.getId()) {
            AnimationSet animationSet2 = new AnimationSet(true);
            ScaleAnimation scaleAnimation2 = new ScaleAnimation(2, 1f, 2, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation2.setDuration(200);
            animationSet2.addAnimation(scaleAnimation2);
            animationSet2.setFillAfter(true);
            pretTab.startAnimation(animationSet2);
        }
        pretTab = view;


    }

    private View pretTab;
}
