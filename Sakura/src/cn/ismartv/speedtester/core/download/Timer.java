package cn.ismartv.speedtester.core.download;

import android.util.Log;
import cn.ismartv.speedtester.AppConstant;

/**
 * Created by huaijie on 12/29/14.
 */

/**
 * 计时器
 */
public class Timer extends Thread {
    private static final String TAG = "Timer";
    public static final int TIME_OVER = 4;
    public volatile int timer = 0;


    @Override
    public void run() {
        while (timer <= TIME_OVER) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                if (AppConstant.DEBUG)
                    e.printStackTrace();
                else
                    Log.e(TAG, e.getMessage());
            }
            timer += 1;
        }
    }


}
