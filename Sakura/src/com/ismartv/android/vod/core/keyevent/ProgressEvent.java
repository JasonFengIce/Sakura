package com.ismartv.android.vod.core.keyevent;

import android.content.Intent;

/**
 * Created by huaijie on 14-11-5.
 */
public class ProgressEvent implements KeyEventInterface {
    @Override
    public void deliverEvent() {
        Intent localIntent = new Intent();
//        localIntent.setAction("android.intent.action.VOD_SET_POSITION");
//        localIntent.putExtra("seqId", 10000);
//        localIntent.putExtra("position", second);
//        context.sendBroadcast(localIntent);
    }
}
