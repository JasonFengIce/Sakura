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
    private RemoteController mRemoteController;

    private static final String S61 = "ideatv S61";
    private static final String S51 = "ideatv S51";
    private static final String K82 = "ideatv K82";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
