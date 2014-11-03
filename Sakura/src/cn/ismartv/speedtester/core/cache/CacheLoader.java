package cn.ismartv.speedtester.core.cache;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by fenghb on 14-7-14.
 */
public class CacheLoader extends CursorLoader {
    private static final String TAG = "CacheLoader";
    private Context context;

    public CacheLoader(Context context) {
        super(context);
        this.context = context;
    }

    public CacheLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        this.context = context;
    }

    @Override
    public Cursor loadInBackground() {
        return super.loadInBackground();
    }
}
