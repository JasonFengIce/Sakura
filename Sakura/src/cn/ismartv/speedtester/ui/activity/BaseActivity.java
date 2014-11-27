package cn.ismartv.speedtester.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteController;
import android.support.v4.app.FragmentActivity;

/**
 * Created by huaijie on 11/27/14.
 */
public class BaseActivity extends FragmentActivity {
    private RemoteController mRemoteController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void hideCursor(boolean hide){
        if(mRemoteController==null){
            mRemoteController = (RemoteController)getSystemService(Context.REMOTECONTROLLER_SERVICE);
        }
        if(hide){
            mRemoteController.setRcGestureOnly();
            mRemoteController.displayCursor(false);
        } else {
            mRemoteController.setDefaultMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideCursor(true);
    }
}
