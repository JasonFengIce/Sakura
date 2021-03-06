package tv.ismar.sakura.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import tv.ismar.sakura.R;


/**
 * Created by huaijie on 10/15/15.
 */
public class AppUpdateMessagePopWindow extends PopupWindow implements View.OnClickListener, View.OnFocusChangeListener, OnHoverListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;

    private Context mContext;
    private View messageLayout;
    private ImageView cursorImageView;
    private int popCursorLeft;
    private int popCursorRight;
    private int popCursorMiddle;
    private LinearLayout appUpdateContentList;

//    private int tmpX = 0;


    public AppUpdateMessagePopWindow(Context context, List<String> msgs, final ConfirmListener confirmListener, final CancelListener cancleListener) {

        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        popCursorLeft = (int) mContext.getResources().getDimension(R.dimen.pop_cursor_left);
        popCursorMiddle = (int) mContext.getResources().getDimension(R.dimen.pop_cursor_middle);
        popCursorRight = (int) mContext.getResources().getDimension(R.dimen.pop_cursor_right);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        int width = (int) (context.getResources().getDimension(R.dimen.message_pop_width));
        int height = (int) (context.getResources().getDimension(R.dimen.app_update_message_pop_height));

        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.sakura_app_update_popup_message, null);
        appUpdateContentList = (LinearLayout) contentView.findViewById(R.id.app_update_content_list);

        appUpdateContentList.removeAllViews();
        for (String s : msgs) {
            TextView textView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.item_app_update, null);
            textView.setText(s);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int) mContext.getResources().getDimension(R.dimen.app_update_item_margin);
            layoutParams.setMargins(margin * 3, margin, margin * 3, margin);
            layoutParams.gravity = Gravity.CENTER;
            appUpdateContentList.addView(textView, layoutParams);
        }

        confirmBtn = (Button) contentView.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) contentView.findViewById(R.id.cancel_btn);
        confirmBtn.setOnHoverListener(this);
        cancelBtn.setOnHoverListener(this);
        messageLayout = contentView.findViewById(R.id.message);
        cursorImageView = (ImageView) contentView.findViewById(R.id.pop_cursor);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnFocusChangeListener(this);
        cancelBtn.setOnFocusChangeListener(this);

        RelativeLayout frameLayout = new RelativeLayout(mContext);
        frameLayout.setBackgroundColor(context.getResources().getColor(R.color.pop_bg));
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.pop_cursor_width), (int) context.getResources().getDimension(R.dimen.pop_cursor_height));
        if (confirmListener != null && cancleListener != null) {
            params.setMargins(popCursorLeft, 0, 0, 0);
        } else {
            params.setMargins(popCursorMiddle, 0, 0, 0);
        }

        cursorImageView.setLayoutParams(params);

        frameLayout.addView(contentView, layoutParams);

        setContentView(frameLayout);
        setFocusable(true);

    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (!v.isFocused()) {
                    v.requestFocusFromTouch();
                    v.requestFocus();
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                messageLayout.requestFocus();
                break;

        }


        return true;
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {

//        int x1 = (int) (mContext.getResources().getDimension(R.dimen.pop_confirm_cursor_x));
//        int x2 = (int) (mContext.getResources().getDimension(R.dimen.pop_cancel_cursor_x));


        switch (v.getId()) {
            case R.id.confirm_btn:
                if (hasFocus) {
                    ((Button) v).setTextColor(mContext.getResources().getColor(R.color._ff9c3c));
//                    slideview(cursorImageView, 0, x1 - v.getX());
                    if (confirmListener != null && cancleListener != null) {
                        layoutCursorView(cursorImageView, popCursorLeft);
                    } else {
                        layoutCursorView(cursorImageView, popCursorMiddle);
                    }

                } else {
                    ((Button) v).setTextColor(mContext.getResources().getColor(R.color._ffffff));

                }
                break;
            case R.id.cancel_btn:
                if (hasFocus) {
                    ((Button) v).setTextColor(mContext.getResources().getColor(R.color._ff9c3c));
                    if (confirmListener != null && cancleListener != null) {
                        layoutCursorView(cursorImageView, popCursorRight);
                    } else {
                        layoutCursorView(cursorImageView, popCursorMiddle);
                    }
//                    slideview(cursorImageView, 0, x2 - v.getX());

                } else {
                    ((Button) v).setTextColor(mContext.getResources().getColor(R.color._ffffff));
                }
                break;
        }

    }

    public void setButtonText(String btn1, String btn2) {
        confirmBtn.setText(btn1);
        cancelBtn.setText(btn2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                if (confirmListener != null) {
                    confirmListener.confirmClick(v);
                }
                break;
            case R.id.cancel_btn:
                if (cancleListener != null) {
                    cancleListener.cancelClick(v);
                }
                break;
        }
    }

    public void showAtLocation(View parent, int gravity) {
//        if (confirmListener == null) {
//            confirmBtn.setVisibility(View.GONE);
//        }
//
//        if (cancleListener == null) {
//            cancelBtn.setVisibility(View.GONE);
//        }
//        this.confirmListener = confirmListener;
//        this.cancleListener = cancleListener;
        super.showAtLocation(parent, gravity, 0, 0);
//        getContentView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (confirmListener != null && cancleListener != null) {
//                    layoutCursorView(cursorImageView, popCursorLeft);
//
//                } else {
//                    layoutCursorView(cursorImageView, popCursorMiddle);
//                }
//            }
//        }, 200);
    }

    public void layoutCursorView(final View view, final int xPosition) {
        int width = view.getWidth();
        int height = view.getHeight();
        int left = xPosition;
        int top = view.getTop();
        view.layout(left, top, left + width, top + height);
    }


    public interface CancelListener {
        void cancelClick(View view);
    }


    public interface ConfirmListener {
        void confirmClick(View view);
    }

//    public void slideview(final View view, final float xPosition,) {
//        TranslateAnimation animation = new TranslateAnimation(xFrom, xTo, 0, 0);
//        animation.setInterpolator(new OvershootInterpolator());
//        animation.setDuration(500);
////        animation.setStartOffset(delayMillis);
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                int left = view.getLeft() + (int) (xTo - xFrom);
//                int top = view.getTop();
//                int width = view.getWidth();
//                int height = view.getHeight();
//                view.clearAnimation();
//                view.layout(left, top, left + width, top + height);
//            }
//        });


//        view.startAnimation(animation);
//}

}
