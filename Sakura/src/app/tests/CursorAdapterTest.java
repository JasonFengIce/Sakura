package app.tests;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huaijie on 12/30/14.
 */
public class CursorAdapterTest extends CursorAdapter {

    public CursorAdapterTest(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public CursorAdapterTest(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
