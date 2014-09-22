package cn.ismartv.speedtester;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import cn.ismartv.speedtester.core.httpclient.NetWorkClient;
import cn.ismartv.speedtester.ui.fragment.NodeFragment;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.Utilities;
import com.ismartv.android.vod.service.HttpProxyService;

/**
 * Created by fenghb on 14-7-7.
 */
public class SakuraApplication extends Application {
    private static final String TAG = "SakuraApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        NetWorkClient.getLatestAppVersion(this);
        NetWorkClient.getTag(this);

        NodeFragment.get(this);
        Utilities.installPackage(this);

        Intent ootStartIntent = new Intent(this, HttpProxyService.class);
        this.startService(ootStartIntent);

        Log.d(TAG, "on create ......");
        Log.d(TAG, "sn code is : " + DevicesUtilities.getSNCode());

    }


}
