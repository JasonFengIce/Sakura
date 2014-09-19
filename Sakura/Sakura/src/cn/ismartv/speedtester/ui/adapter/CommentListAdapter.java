package cn.ismartv.speedtester.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.data.Comment;

import java.util.List;

/**
 * Created by <huaijiefeng@gmail.com> on 8/28/14.
 */
public class CommentListAdapter extends BaseAdapter {
    private Context context;
    private List<Comment.Data> list;

    public CommentListAdapter(Context context, List<Comment.Data> list) {
        this.context = context;
        this.list = list;

    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_comment, null);
            holder.submitTimeText = (TextView) convertView.findViewById(R.id.submit_time);
            holder.comment = (TextView) convertView.findViewById(R.id.commont);
            holder.replyTime = (TextView) convertView.findViewById(R.id.reply_time);
            holder.reply = (TextView) convertView.findViewById(R.id.reply);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.submitTimeText.setText(list.get(position).getSubmit_time());
        holder.comment.setText(list.get(position).getCommont());
        holder.replyTime.setText(list.get(position).getReply_time());
        holder.reply.setText(list.get(position).getReply());
        return convertView;

    }


    class ViewHolder {
        private TextView submitTimeText;
        private TextView comment;
        private TextView replyTime;
        private TextView reply;
    }
}
