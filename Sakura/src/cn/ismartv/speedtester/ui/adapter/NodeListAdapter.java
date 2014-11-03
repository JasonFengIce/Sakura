package cn.ismartv.speedtester.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.ui.widget.progressbar.SakuraProgressBar;

/**
 * Created by huaijie on 14-10-31.
 */
public class NodeListAdapter extends CursorAdapter {
    public NodeListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public NodeListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public NodeListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_node_list, null);


        return mView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nodeNmae = (TextView) view.findViewById(R.id.node_name);
        TextView titleNumber = (TextView) view.findViewById(R.id.title_number);
        SakuraProgressBar speedProgress = (SakuraProgressBar) view.findViewById(R.id.speed_progress);


        titleNumber.setText(String.valueOf(cursor.getPosition() + 1));
        String node = cursor.getString(cursor.getColumnIndex("nick"));
        int progress = cursor.getInt(cursor.getColumnIndex(NodeCacheTable.SPEED));
        speedProgress.setProgress((int) (progress / 20.84));
        nodeNmae.setText(node);

    }
}
