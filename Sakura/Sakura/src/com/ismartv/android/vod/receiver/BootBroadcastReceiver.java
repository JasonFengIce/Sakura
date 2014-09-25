package com.ismartv.android.vod.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.httpclient.Utils;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.Utilities;
import com.ismartv.android.vod.service.HttpProxyService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "action --> " + intent.getAction());
        if (intent.getAction().equals(ACTION_BOOT)) {

            Utilities.updateApp(context);
            Utilities.installPackage(context);
            Intent ootStartIntent = new Intent(context, HttpProxyService.class);
            context.startService(ootStartIntent);
            new Thread()
            {
                @Override
                public void run() {
                    try {
                        sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    CacheManager.updatLocalIp(context, DevicesUtilities.getLocalIpAddressV4());
                }
            }.start();

        }
    }




}
