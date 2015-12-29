package cn.ismartv.iris;

/**
 * Created by huaijie on 3/9/15.
 */
public class AppConstant {
    public static final AppFlavor APP_FLAVOR = AppFlavor.Tencent;

    public static final String KIND = "sky";
    public static final String VERSION = "1.0";
    public static final String MANUFACTURE = "sky";

    public static final String APP_UPDATE_ACTION = "cn.ismartv.vod.action.app_update";

    public static final boolean DEBUG = true;



    enum AppFlavor {
        Tencent,
        Sharp
    }
}
