package ismartv.cn.sakura.test.core;

import android.test.AndroidTestCase;
import android.util.Log;
import cn.ismartv.speedtester.utils.StringUtilities;

/**
 * Created by fenghb on 14-7-16.
 */
public class StringTest extends AndroidTestCase {
    private static final String TAG = "StringTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getByNode();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void getByNode() {
        int i = StringUtilities.getAreaCodeByNode("华东电信14");
        Log.d(TAG, "area code is : " + i);
    }

}
