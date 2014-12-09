package cn.ismartv.speedtester.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.data.ChatMsgEntity;
import cn.ismartv.speedtester.utils.DeviceUtils;

import java.util.List;

/**
 * Created by huaijie on 14-10-29.
 */
public class FeedbackListAdapter extends BaseAdapter {
    private Context context;
    private List<ChatMsgEntity.Data> list;


    public FeedbackListAdapter(Context context, List<ChatMsgEntity.Data> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_feedback_list, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.feedbackTime.setText(list.get(position).getSubmit_time());
        holder.feedbackCustomer.setText(DeviceUtils.getModel() + " : " + list.get(position).getCommont());
        holder.feedbackIsmartv.setText(context.getText(R.string.ismartv) + list.get(position).getReply());
        return view;
    }


    static class ViewHolder {
        @InjectView(R.id.feedback_time)
        TextView feedbackTime;

        @InjectView(R.id.feedback_customer)
        TextView feedbackCustomer;

        @InjectView(R.id.feedback_ismartv)
        TextView feedbackIsmartv;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
