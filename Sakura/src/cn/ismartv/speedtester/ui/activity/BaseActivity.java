package cn.ismartv.speedtester.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteController;
import android.support.v4.app.FragmentActivity;
import cn.ismartv.speedtester.utils.DeviceUtils;

/**
 * Created by huaijie on 11/27/14.
 */
public class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";

    private RemoteController mRemoteController;

    private static final String S61 = "ideatv S61";
    private static final String S51 = "ideatv S51";
    private static final String K82 = "ideatv K82";

    public TVModel tvModel;
    /**
     * The system broadcast intent string
     */
    public static final String ACTION_LAUNCHER = "com.lenovo.dll.nebula.launcher.home";
    public static final String ACTION_SETTING = "com.lenovo.nebula.settings.action.launch";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DeviceUtils.getModel().equals(S51) || DeviceUtils.getModel().equals(S61) || DeviceUtils.getModel().equals(K82))
            tvModel = TVModel.S51;
        else
            tvModel = TVModel.S52;


//        registerReceiver(mCloseReceiver, new IntentFilter(ACTION_LAUNCHER));
//        registerReceiver(mCloseReceiver, new IntentFilter(ACTION_SETTING));
    }

    public void hideCursor(boolean hide) {
        if (mRemoteController == null) {
            mRemoteController = (RemoteController) getSystemService(Context.REMOTECONTROLLER_SERVICE);
        }
        if (hide) {
            mRemoteController.setRcGestureOnly();
            mRemoteController.displayCursor(false);
        } else {
            mRemoteController.setDefaultMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DeviceUtils.getModel().equals(S51) || DeviceUtils.getModel().equals(S61) || DeviceUtils.getModel().equals(K82))
            hideCursor(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
//        unregisterReceiver(mCloseReceiver);
        super.onDestroy();

    }

    public enum TVModel {
        S51, S52
    }


//    private BroadcastReceiver mCloseReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "onReceive ---> " + intent.getAction());
//            BaseActivity.this.finish();
//        }
//    };


}
