package com.ismartv.android.vod.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.utils.DeviceUtils;
import com.activeandroid.query.Delete;
import com.ismartv.android.vod.core.keyevent.EventDeliver;
import com.ismartv.android.vod.core.keyevent.KeyEventInterface;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

/**
 * Created by huaijie on 14-7-31.
 */


public class HttpProxyService extends Service implements HttpServerRequestCallback {

    private static final int BUTTON_KEY_EVENT = 1;
    private static final int VOL_SEEK_EVENT = 2;
    private static final int PLAY_VIDEO_EVENT = 3;
    private static final int DELETE_CDN = 4;

    private static final String TAG = "HttpProxyService";
    private static final int PORT = 10114;
    private static final String HTTP_ACTIOIN = "/keyevent";
    private static final String PING = "/ping";
    private static final String MODEL = "/model";
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
        server.post(PING, this);
        server.post(MODEL, this);
        server.get(HTTP_ACTIOIN, this);
        server.listen(PORT);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        if (AppConstant.DEBUG)
            android.util.Log.d(TAG, "path is ---> " + request.getPath());
        if (MODEL.equals(request.getPath())) {
            response.getHeaders().getHeaders().add("Access-Control-Allow-Origin", "*");
            response.send(DeviceUtils.getModel());
            response.writeHead();
        } else if (PING.equals(request.getPath())) {
            response.getHeaders().getHeaders().add("Access-Control-Allow-Origin", "*");
            response.send("OK!");
            response.writeHead();
        } else if (HTTP_ACTIOIN.equals(request.getPath())) {

            int actionCode = Integer.parseInt(request.getQuery().getString("action"));
            KeyEventInterface keyEventInterface = null;
            switch (actionCode) {
                case BUTTON_KEY_EVENT:
                    keyEventInterface = EventDeliver.create(getApplicationContext(), actionCode, request.getQuery().getString("keycode"), nativeservice);
                    break;
                case VOL_SEEK_EVENT:
                    keyEventInterface = EventDeliver.create(getApplicationContext(), actionCode, request.getQuery().getString("seek"), nativeservice);
                    break;
                case PLAY_VIDEO_EVENT:
                    String url = request.getQuery().getString("url");
                    String pk = request.getQuery().getString("pk");
                    Log.d(TAG, "url is ---> " + url);
                    Log.d(TAG, "code is ---> " + pk);

                    Intent intent = new Intent();

                    intent.putExtra("ItemUrl", url);
                    intent.putExtra("Code", pk);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("ContentModel", "teleplay");
                    intent.putExtra("ModuleName", "4S");
                    intent.setClassName("com.lenovo.dll.nebula.vod", "com.lenovo.dll.nebula.vod.player.VODPlayerActivity");
                    startActivity(intent);


                    break;
                case DELETE_CDN:
                    Log.d(TAG, "execute delete cdn");
                    new Delete().from(NodeCacheTable.class).execute();
                    break;
                default:
                    break;
            }
            keyEventInterface.deliverEvent();
            response.send("OK!");

        }
        response.end();
    }

    @Override
    public void onDestroy() {
        server.stop();
        unbindService(mConnection);
        super.onDestroy();
    }
}