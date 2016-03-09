package tv.ismar.sakura.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.sakura.R;
import tv.ismar.sakura.data.table.CdnTable;
import tv.ismar.sakura.data.table.IspTable;
import tv.ismar.sakura.ui.widget.SakuraProgressBar;

/**
 * Created by huaijie on 14-10-31.
 */
public class NodeListAdapter extends CursorAdapter implements View.OnHoverListener {
    private ListView listView;
    private View selectedView;

    public NodeListAdapter(Context context, Cursor c, boolean autoRequery, ListView listView) {
        super(context, c, autoRequery);
        this.listView = listView;
    }

    public NodeListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void resetSelectedView() {
        if (selectedView != null) {
            selectedView.setSelected(false);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View mView = LayoutInflater.from(context).inflate(R.layout.sakura_item_node_list, null);

        return mView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor.getCount() != 0) {

            TextView nodeNmae = (TextView) view.findViewById(R.id.node_name);
            TextView titleNumber = (TextView) view.findViewById(R.id.title_number);
            TextView message = (TextView) view.findViewById(R.id.select_prompt);
            SakuraProgressBar speedProgress = (SakuraProgressBar) view.findViewById(R.id.speed_progress);
            titleNumber.setText(String.valueOf(cursor.getPosition() + 1));
            String node = cursor.getString(cursor.getColumnIndex(CdnTable.CDN_NICK));
            int progress = cursor.getInt(cursor.getColumnIndex(CdnTable.SPEED));
            String ispId = cursor.getString(cursor.getColumnIndex(CdnTable.ISP_ID));
            IspTable ispTable = new Select().from(IspTable.class).where(IspTable.ISP_ID + " = ?", ispId).executeSingle();
            speedProgress.setProgress((int) (progress / 20.84));
            if ((progress / 20.84) < 60 || ispTable.isp_name.equals("其它"))
                message.setText(R.string.tring);
            else
                message.setText(R.string.can_select);
            nodeNmae.setText(node);
            view.setTag((cursor.getInt(cursor.getColumnIndex(CdnTable.CDN_ID))));
            view.setTag(R.id.list_item_position, cursor.getPosition());
            view.setOnHoverListener(this);
        }
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                listView.requestFocusFromTouch();
                listView.getSelectedView().setSelected(false);
                v.setSelected(true);
                selectedView = v;
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                v.setSelected(false);
                break;
        }
        return true;
    }

}
