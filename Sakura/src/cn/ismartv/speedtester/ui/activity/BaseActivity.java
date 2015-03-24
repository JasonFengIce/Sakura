package cn.ismartv.speedtester.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import cn.ismartv.speedtester.utils.DeviceUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by huaijie on 11/27/14.
 */
public class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";


    private static final String S61 = "ideatv S61";
    private static final String S51 = "ideatv S51";
    private static final String K82 = "ideatv K82";
    private static final String S9i = "LenovoTV 55S9i";

    public TVModel tvModel;
    /**
     * The system broadcast intent string
     */
    public static final String ACTION_LAUNCHER = "com.lenovo.dll.nebula.launcher.home";
    public static final String ACTION_SETTING = "com.lenovo.nebula.settings.action.launch";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DeviceUtils.getModel().equals(S9i)) {
            tvModel = TVModel.S9i;
        } else if (DeviceUtils.getModel().equals(S51) || DeviceUtils.getModel().equals(S61) || DeviceUtils.getModel().equals(K82))
            tvModel = TVModel.S51;
        else
            tvModel = TVModel.S52;


//        registerReceiver(mCloseReceiver, new IntentFilter(ACTION_LAUNCHER));
//        registerReceiver(mCloseReceiver, new IntentFilter(ACTION_SETTING));
    }


    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();

    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public enum TVModel {
        S51, S52, S9i
    }
}
