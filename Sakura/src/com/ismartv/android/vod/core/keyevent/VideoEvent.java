package com.ismartv.android.vod.core.keyevent;

import android.content.Context;
import android.content.Intent;
import com.activeandroid.util.Log;

/**
 * Created by huaijie on 14-11-5.
 */
public class VideoEvent implements KeyEventInterface {
    private static final String TAG = "VideoEvent";

    private String params;
    private Context context;

    public VideoEvent(String params, Context context) {
        this.params = params;
        this.context = context;
    }


    @Override
    public void deliverEvent() {
        Intent intent = new Intent();
        intent.putExtra("Code", "387068");
        android.util.Log.d(TAG, "url is ---> " + params);
        intent.putExtra("ItemUrl", params);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ContentModel", "teleplay");
        intent.putExtra("ModuleName", "4S");
        intent.setClassName("com.lenovo.dll.nebula.vod", "com.lenovo.dll.nebula.vod.player.VODPlayerActivity");
        context.startActivity(intent);
    }
}
