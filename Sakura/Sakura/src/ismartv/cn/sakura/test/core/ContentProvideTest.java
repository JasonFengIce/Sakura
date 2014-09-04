package ismartv.cn.sakura.test.core;

import android.content.ContentValues;
import android.test.AndroidTestCase;
import cn.ismartv.speedtester.provider.NodeCache;

/**
 * Created by fenghb on 14-7-16.
 */
public class ContentProvideTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NodeCache.SPEED, "122.225");
        getContext().getContentResolver().update(NodeCache.CONTENT_URI, contentValues, NodeCache.CDN_ID, new String[]{"11"});
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
