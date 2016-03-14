package tv.ismar.sakura.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import tv.ismar.sakura.R;
import tv.ismar.sakura.data.table.IspTable;

/**
 * Created by huaijie on 3/14/16.
 */
public class IspSpinnerPopWindow extends PopupWindow implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener {
    private static final String TAG = "SpinnerPopWindow";
    private OnItemClickListener itemClickListener;


    public IspSpinnerPopWindow(Context context, List<IspTable> list, OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        setWidth((int) context.getResources().getDimension(R.dimen.pop_spinner_layout_width));
        setHeight((int) context.getResources().getDimension(R.dimen.pop_spinner_layout_height));

        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_spinner, null);
        LinearLayout popListLayout = (LinearLayout) contentView.findViewById(R.id.spinner_list_layout);

        for (int i = 0; i < list.size(); i++) {
            TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.item_pop_spinner, null);
            textView.setText(list.get(i).isp_name);
            textView.setOnClickListener(this);
            textView.setOnFocusChangeListener(this);
            textView.setOnHoverListener(this);
            textView.setTag(list.get(i));
            popListLayout.addView(textView);
        }

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pop_bg));
        setFocusable(true);

        setContentView(contentView);
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onItemClick(v);
        dismiss();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

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
                break;
        }
        return true;
    }


    public interface OnItemClickListener {
        void onItemClick(View itemView);
    }

}
