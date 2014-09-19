package ismartv.android.vod.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import ismartv.android.vod.core.NetWorkUtil;

/**
 * Created by <huaijiefeng@gmail.com> on 9/3/14.
 */
public class NetWorkBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NetWorkBroadcastReceiver";
    NetworkInfo.State wifiState = null;
    NetworkInfo.State mobileState = null;
    public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (ACTION.equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
                Log.d(TAG, "mobile network connect success!!!");
                NetWorkUtil.getInstant().weiXinUpload(context);
            } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED == wifiState && NetworkInfo.State.CONNECTED != mobileState) {
                Log.d(TAG, "wifi connect success!!!");
                NetWorkUtil.getInstant().weiXinUpload(context);
            } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
                Log.d(TAG, "can't connect network!!!");
            }
        }
    }
}
