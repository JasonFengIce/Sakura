package cn.ismartv.speedtester.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by huaijie on 14-7-31.
 */
public class Message {
    private static final String TAG = Message.class.getSimpleName();

    public static final String STATUS = "status";
    public static final int COMPLETE = 0x0000;
    public static final int RUNNING = 0x0001;

    public static final String ACTION = "cn.ismartv.sakura.download";

    public static void sendMessage(Context context, int status) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(STATUS, status);
        context.sendBroadcast(intent);
    }
}
