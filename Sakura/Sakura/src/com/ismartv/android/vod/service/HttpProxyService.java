package com.ismartv.android.vod.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huaijie.tools.net.async.AsyncServer;
import com.huaijie.tools.net.async.http.server.AsyncHttpServer;
import com.huaijie.tools.net.async.http.server.AsyncHttpServerRequest;
import com.huaijie.tools.net.async.http.server.AsyncHttpServerResponse;
import com.huaijie.tools.net.async.http.server.HttpServerRequestCallback;
import com.ismartv.android.vod.core.RemoteControl;

/**
 * Created by huaijie on 14-7-31.
 */


public class HttpProxyService extends Service implements HttpServerRequestCallback {
    private static final String TAG = "HttpProxyService";
    private static final int ACTION_KEY_EVNET = 1;
    private static final int ACTION_SEEK_EVNET = 2;
    private static final int ACTION_PLAY_VIDEO = 3;
    private static final String HTTP_ACTIOIN = "/keyevent";

    private AsyncHttpServer server;
    private ISmartvNativeService nativeservice;

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "HttpProxyService onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "HttpProxyService onCreate");
        super.onCreate();
        Intent intent = new Intent("com.ismartv.android.vod.service.keymonitor");
        server = new AsyncHttpServer();
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "HttpProxyService onStartCommand");
        server.get(HTTP_ACTIOIN, this);
        server.listen(AsyncServer.getDefault(), 5000);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

        int actionCode = Integer.parseInt(request.getQuery().getString("action"));

        switch (actionCode) {
            case ACTION_KEY_EVNET:
                sendKeyEvent(Integer.parseInt(request.getQuery().getString("keycode")));
                break;
            case ACTION_SEEK_EVNET:
                RemoteControl.seekVolume(this, Integer.parseInt(request.getQuery().getString("seek")));
                break;
            case ACTION_PLAY_VIDEO:
                RemoteControl.play(this, request.getQuery().getString("url"));
                break;
            default:
                break;
        }

        response.send("Hello!!!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
        unbindService(mConnection);

    }

    private void sendKeyEvent(int keyEventCode) {
        Log.d(TAG, "key evnet code --> " + keyEventCode);
        try {
            nativeservice.sendMoniterKey(keyEventCode);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            nativeservice = ISmartvNativeService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };

}
