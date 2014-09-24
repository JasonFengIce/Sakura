package cn.ismartv.speedtester;

import android.app.Application;
import android.content.Intent;
import com.ismartv.android.vod.service.HttpProxyService;

/**
 * Created by fenghb on 14-7-7.
 */
public class SakuraApplication extends Application {
    private static final String TAG = "SakuraApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Intent ootStartIntent = new Intent(this, HttpProxyService.class);
        this.startService(ootStartIntent);
    }
}
