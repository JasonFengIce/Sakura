package com.ismartv.android.vod.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.ismartv.android.vod.core.keyevent.EventDeliver;
import com.ismartv.android.vod.core.keyevent.KeyEventInterface;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import org.apache.http.NameValuePair;

import java.util.Iterator;

/**
 * Created by huaijie on 14-7-31.
 */


public class HttpProxyService extends Service implements HttpServerRequestCallback {

    private static final String TAG = "HttpProxyService";
    private static final int PORT = 9114;
    private static final int DEFAULT_VALUE = 1;
    private static final int MAX_CHECK_TIME = 3;

    private static final String HTTP_ACTIOIN = "/keyevent";


    private AsyncHttpServer server;

    private ISmartvNativeService nativeservice;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            nativeservice = ISmartvNativeService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent("com.ismartv.android.vod.service.keymonitor");
        server = new AsyncHttpServer();
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        server.get(HTTP_ACTIOIN, this);
        server.listen(AsyncServer.getDefault(), PORT);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        Iterator<NameValuePair> iterator = request.getQuery().iterator();
        int actionCode = Integer.parseInt(iterator.next().getValue());
        String params = iterator.next().getValue();
        KeyEventInterface keyEventInterface = EventDeliver.create(getApplicationContext(), actionCode, params);
        keyEventInterface.deliverEvent();
        response.send("OK!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
        unbindService(mConnection);

    }

    private void sendKeyEvent(int keyEventCode) {
        try {
            nativeservice.sendMoniterKey(keyEventCode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}