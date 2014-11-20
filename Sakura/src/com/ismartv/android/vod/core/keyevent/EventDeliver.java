package com.ismartv.android.vod.core.keyevent;

import android.content.Context;
import com.ismartv.android.vod.service.ISmartvNativeService;

/**
 * Created by huaijie on 14-11-5.
 */
public class EventDeliver {

    public static final int ACTION_KEY_EVNET = 1;
    public static final int ACTION_VOLUME_EVNET = 2;
    public static final int ACTION_PLAY_VIDEO = 3;
    public static final int PING = 4;

    public static KeyEventInterface create(Context context, int evnetCode, String params, ISmartvNativeService service) {
        KeyEventInterface keyEventInterface = null;
        switch (evnetCode) {
            case ACTION_KEY_EVNET:
                keyEventInterface = new ButtonEvent(params,service);
                break;
            case ACTION_VOLUME_EVNET:
                keyEventInterface = new VolumeEvent(params, context);
                break;
            case ACTION_PLAY_VIDEO:
                keyEventInterface = new VideoEvent(params, context);
                break;
            case PING:
                break;
        }
        return keyEventInterface;
    }
}
