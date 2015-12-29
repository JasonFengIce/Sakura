package cn.ismartv.iris.ui.provider;

import android.content.ContentValues;
import android.net.Uri;
import com.activeandroid.content.ContentProvider;

/**
 * Created by admin on 2015/12/29.
 */
public class IrisContentProvider extends ContentProvider{

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return super.insert(uri, values);
    }
}
