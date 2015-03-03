package com.ismartv.android.vod.core.keyevent;

import android.content.Context;
import android.content.Intent;
import com.activeandroid.util.Log;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
    }
}
