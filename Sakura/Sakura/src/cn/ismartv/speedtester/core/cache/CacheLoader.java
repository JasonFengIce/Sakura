package cn.ismartv.speedtester.core.cache;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

/**
 * Created by fenghb on 14-7-14.
 */
public class CacheLoader extends CursorLoader {
    private static final String TAG = CacheLoader.class.getSimpleName();
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
