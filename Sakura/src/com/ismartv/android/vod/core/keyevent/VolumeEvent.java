package com.ismartv.android.vod.core.keyevent;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by huaijie on 14-11-5.
 */
public class VolumeEvent implements KeyEventInterface {
    private int params;
    private Context context;

    public VolumeEvent(String params, Context context) {
        this.params = Integer.parseInt(params);
        this.context = context;
    }

    @Override
    public void deliverEvent() {
        AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (params / 100.0 * max), AudioManager.FLAG_SHOW_UI);
    }
}
