package com.ismartv.android.vod.core.keyevent;

import android.content.Context;
import android.content.Intent;

/**
 * Created by huaijie on 14-11-5.
 */
public class VideoEvent implements KeyEventInterface {

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
        intent.putExtra("ItemUrl", params);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ContentModel", "teleplay");
        intent.putExtra("ModuleName", "4S");
        intent.setClassName("com.lenovo.dll.nebula.vod", "com.lenovo.dll.nebula.vod.player.VODPlayerActivity");
        context.startActivity(intent);
    }
}
