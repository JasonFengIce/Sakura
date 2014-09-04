package ismartv.cn.sakura.test.core;

import android.test.AndroidTestCase;
import android.util.Log;
import cn.ismartv.speedtester.utils.DevicesUtilities;

/**
 * Created by fenghb on 14-7-11.
 */
public class DevicesTest extends AndroidTestCase {
    private static final String TAG = "DevicesTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getSNCode();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void getSNCode() {
        Log.d(TAG, DevicesUtilities.getSNCode());
    }
}
