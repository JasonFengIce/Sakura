package com.ismartv.android.vod.core;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by <huaijiefeng@gmail.com> on 8/15/14.
 */
public class RemoteControl {
    private static final String TAG = "RemoteControl";

    public static void seekProgress(Context context, int second) {
        Intent localIntent = new Intent();
        localIntent.setAction("android.intent.action.VOD_SET_POSITION");
        localIntent.putExtra("seqId", 10000);
        localIntent.putExtra("position", second);
        context.sendBroadcast(localIntent);
    }

    public static void seekVolume(Context context, int volume) {
        AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (volume / 100.0 * max), AudioManager.FLAG_SHOW_UI);
    }

    public static void play(Context context, String url) {
        Intent intent = new Intent();
        intent.putExtra("Code", "387068");
        intent.putExtra("ItemUrl", url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ContentModel", "teleplay");
        intent.putExtra("ModuleName", "4S");
        intent.setClassName("com.lenovo.dll.nebula.vod", "com.lenovo.dll.nebula.vod.player.VODPlayerActivity");
        context.startActivity(intent);
    }
}
