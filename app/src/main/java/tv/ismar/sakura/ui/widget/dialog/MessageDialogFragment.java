package tv.ismar.sakura.ui.widget.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.sakura.R;


/**
 * Created by huaijie on 10/15/15.
 */
public class MessageDialogFragment extends PopupWindow implements View.OnClickListener, View.OnHoverListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;
    private ImageView tmpImage;

    private String mFirstLineMessage;
    private String mSecondLineMessage;

    private Context mContext;


    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocusFromTouch();
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmpImage.requestFocus();
                break;
        }
        return true;
    }

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }


    public MessageDialogFragment(Context context, String line1Message, String line2Message) {
        mFirstLineMessage = line1Message;
        mSecondLineMessage = line2Message;
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();


        int width = (int) (context.getResources().getDimension(R.dimen.pop_width));
        int height = (int) (context.getResources().getDimension(R.dimen.pop_height));

        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_message, null);
        confirmBtn = (Button) contentView.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) contentView.findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnHoverListener(this);
        cancelBtn.setOnHoverListener(this);
        confirmBtn.requestFocusFromTouch();
        confirmBtn.requestFocus();

        firstMessage = (TextView) contentView.findViewById(R.id.first_text_info);
        secondMessage = (TextView) contentView.findViewById(R.id.pop_second_text);
        tmpImage = (ImageView) contentView.findViewById(R.id.tmp_image);
        firstMessage.setText(mFirstLineMessage);

        RelativeLayout frameLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;

        if (TextUtils.isEmpty(mSecondLineMessage)) {
            layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        } else {
            int doubleLineHeight = (int) (context.getResources().getDimension(R.dimen.pop_double_line_height));
            layoutParams = new RelativeLayout.LayoutParams(width, doubleLineHeight);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            secondMessage.setVisibility(View.VISIBLE);
            secondMessage.setText(mSecondLineMessage);
        }


//        frameLayout.addView(contentView, frameLayout);
        frameLayout.addView(contentView, layoutParams);
//        frameLayout.setBackgroundResource(R.drawable.popwindow_bg);
        setContentView(frameLayout);
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pop_bg_drawable));
        setFocusable(true);

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


    public void showAtLocation(View parent, int gravity, ConfirmListener confirmListener,
                               CancelListener cancleListener) {
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        super.showAtLocation(parent, gravity, 0, 0);
    }
}
