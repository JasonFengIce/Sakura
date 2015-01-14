package cn.ismartv.preferences;

import android.content.Context;
import android.util.Log;

/**
 * Created by huaijie on 12/10/14.
 */
public class PreferenceInitializer {
    private static final String TAG = PreferenceInitializer.class.getSimpleName();
    public static final boolean DEBUG = true;
    private static PreferenceModelInfo sModelInfo;
    private static Context sContext;
    private static boolean sIsInitialized = false;


    public static void initialize(Context context) {
        if (sIsInitialized) {
            return;
        }
        sContext = context;
        sModelInfo = new PreferenceModelInfo(sContext);
        sIsInitialized = true;
    }


    public static PreferenceInfo getPreferenceInfo(Class<? extends PreferenceModel> type) {
        return sModelInfo.getPreferenceInfo(type);
    }

    public static Context getContext() {
        if (null == sContext) {
            Log.d(TAG, "getContext return is null");
        }
        return sContext;
    }


}
