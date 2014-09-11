package cn.ismartv.speedtester;

import android.app.Application;
import android.util.Log;
import cn.ismartv.speedtester.core.httpclient.NetWorkClient;
import cn.ismartv.speedtester.ui.fragment.NodeFragment;
import cn.ismartv.speedtester.utils.DevicesUtilities;

/**
 * Created by fenghb on 14-7-7.
 */
public class SakuraApplication extends Application {
    private static final String TAG = "SakuraApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "on create ......");
        Log.d(TAG, "sn code is : " + DevicesUtilities.getSNCode());
        NetWorkClient.getTag(this);
        NetWorkClient.getLatestAppVersion(this);
        NodeFragment.getCurrentCdn(this);

    }


}
