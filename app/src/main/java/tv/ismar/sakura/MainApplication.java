package tv.ismar.sakura;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.sakura.core.initialization.InitializeProcess;


public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this, true);
        new Thread(new InitializeProcess(this)).start();

    }


    public float getRate(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        return rate;
    }


}
