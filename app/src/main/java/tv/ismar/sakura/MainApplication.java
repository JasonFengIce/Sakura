package tv.ismar.sakura;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.sakura.core.initialization.InitializeProcess;


public class MainApplication extends Application {
    private static String locationPY;
    private static String snToken;
    private static String appUpdateDomain;
    private static String apiDomain;
    private static long geoId;
    private static String city;

    public static String getLocationPY() {
        return locationPY;
    }

    public static Long getGeoId() {
        return geoId;
    }

    public static String getSnToken() {
        return snToken;
    }

    public static String getAppUpdateDomain() {
        return appUpdateDomain;
    }

    public static String getCity() {
        return city;
    }

    public static String getApiDomain() {
        return apiDomain;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this, true);
        initializeInfo();
        new Thread(new InitializeProcess(this)).start();

    }

    public float getRate(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        return rate;
    }

    private void initializeInfo() {
        try {
            Context daisyContext = createPackageContext("tv.ismar.daisy", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sharedPreferences = daisyContext.getSharedPreferences("account", Context.MODE_WORLD_READABLE);
            appUpdateDomain = sharedPreferences.getString("app_update_domain", "http://skytest.tvxio.com");
            locationPY = sharedPreferences.getString("province_py", "");
            snToken = sharedPreferences.getString("sn_token", "");
            apiDomain = sharedPreferences.getString("api_domain", "http://skytest.tvxio.com");
            geoId = Long.parseLong(sharedPreferences.getString("geo_id", "0"));
            city = sharedPreferences.getString("city", "");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
