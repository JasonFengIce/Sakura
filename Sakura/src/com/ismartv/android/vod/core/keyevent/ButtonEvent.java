package com.ismartv.android.vod.core.keyevent;

import android.os.RemoteException;
import android.util.Log;
import com.ismartv.android.vod.service.ISmartvNativeService;

/**
 * Created by huaijie on 11/19/14.
 */
public class ButtonEvent implements KeyEventInterface {
    private static final String TAG = "ButtonEvent";
    private String params;
    private ISmartvNativeService service;

    public ButtonEvent(String params, ISmartvNativeService service) {
        this.params = params;
        this.service = service;
    }

    @Override
    public void deliverEvent() {
        try {
            service.sendMoniterKey(Integer.parseInt(params));
        } catch (RemoteException e) {
            Log.e(TAG, "button event send failed!!!");
        }
    }
}
