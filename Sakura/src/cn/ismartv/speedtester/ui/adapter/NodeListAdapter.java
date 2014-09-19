package cn.ismartv.speedtester.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.provider.NodeCache;
import cn.ismartv.speedtester.ui.widget.progressbar.SakuraProgressBar;

import java.util.HashMap;

/**
 * Created by fenghb on 14-6-24.
 */
public class NodeListAdapter extends CursorAdapter {
    private static final String TAG = NodeListAdapter.class.getSimpleName();
    private Context context;
    private TextView textView;


    public NodeListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.context = context;

    }

    public NodeListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_node_list, null);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //view
        TextView nodeName = (TextView) view.findViewById(R.id.node_name);
        SakuraProgressBar nodeSpeed = (SakuraProgressBar) view.findViewById(R.id.node_speed);
        RadioButton nodeCheck = (RadioButton) view.findViewById(R.id.node_check);
        ProgressBar testProgress = (ProgressBar) view.findViewById(R.id.test_progress);
        TextView testText = (TextView) view.findViewById(R.id.test_running);
        Button select = (Button) view.findViewById(R.id.select_btn);
        ImageView fork = (ImageView) view.findViewById(R.id.fork_image);


        //data from cursor
        String node = cursor.getString(cursor.getColumnIndex(NodeCache.NICK));
        int speed = cursor.getInt(cursor.getColumnIndex(NodeCache.SPEED));
        String checked = cursor.getString(cursor.getColumnIndex(NodeCache.CHECKED));
        String cdnId = cursor.getString(cursor.getColumnIndex(NodeCache.CDN_ID));
        String running = cursor.getString(cursor.getColumnIndex(NodeCache.RUNNING));
        if (speed == -1) {
            fork.setVisibility(View.VISIBLE);
            select.setVisibility(View.INVISIBLE);
        } else {
            select.setVisibility(View.VISIBLE);
            fork.setVisibility(View.INVISIBLE);
        }

        //node name
        nodeName.setText(node);
        //node speed
        nodeSpeed.setProgress((int) (speed / 20.48));
        //node check
        nodeCheck.setChecked("true".equals(checked) ? true : false);

        //node running
        testProgress.setVisibility("true".equals(running) ? View.VISIBLE : View.INVISIBLE);
        testText.setVisibility("true".equals(running) ? View.VISIBLE : View.INVISIBLE);


        HashMap<String, String> map = new HashMap<String, String>();
        map.put("cdn_id", cdnId);
        map.put("node", node);
        select.setTag(map);
        if (checked.equals("true") ? true : false)

            textView.setText(node);


    }


    public void setCurrentNode(TextView textView) {
        this.textView = textView;
    }


}
