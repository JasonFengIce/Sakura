package com.ismartv.android.vod.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.httpclient.BaseClient;
import cn.ismartv.speedtester.data.VersionInfo;
import cn.ismartv.speedtester.utils.Utilities;
import com.huaijie.tools.net.async.AsyncServer;
import com.huaijie.tools.net.async.http.server.AsyncHttpServer;
import com.huaijie.tools.net.async.http.server.AsyncHttpServerRequest;
import com.huaijie.tools.net.async.http.server.AsyncHttpServerResponse;
import com.huaijie.tools.net.async.http.server.HttpServerRequestCallback;
import com.ismartv.android.vod.core.RemoteControl;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by huaijie on 14-7-31.
 */


public class HttpProxyService extends Service implements HttpServerRequestCallback {

    private static final String TAG = "HttpProxyService";
    private static final int DEFAULT_VALUE = 1;
    private static final int MAX_CHECK_TIME = 3;
    private static final int ACTION_KEY_EVNET = 1;
    private static final int ACTION_SEEK_EVNET = 2;
    private static final int ACTION_PLAY_VIDEO = 3;
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

    private void downloadAPK(final Context context, final String downloadUrl, final String md5, final int count) {
    	new Thread() {
			@Override
			public void run() {
				if (count > MAX_CHECK_TIME)
					return;
				File fileName = null;
				try {
					int byteread;
					URL url = new URL(downloadUrl);
					fileName = new File(
							context.getFilesDir().getAbsolutePath(),
							Utilities.APP_NAME);
					if (!fileName.exists())
						fileName.createNewFile();
					URLConnection conn = url.openConnection();
					InputStream inStream = conn.getInputStream();
					FileOutputStream fs = context.openFileOutput(
							Utilities.APP_NAME, Context.MODE_WORLD_READABLE);
					byte[] buffer = new byte[1024];
					while ((byteread = inStream.read(buffer)) != -1) {
						fs.write(buffer, 0, byteread);
					}
					fs.flush();
					fs.close();
					inStream.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String MD5Value = Utilities.getMd5ByFile(fileName);
				if (md5.equals(MD5Value)) {
					CacheManager.updateVersion(context, 1, Utilities.APP_NAME);
				} else {
					downloadAPK(context, downloadUrl, md5, count + 1);
				}
			}
		}.start();
    }

    public void getLatestAppVersion(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .setEndpoint(BaseClient.HOST)
                .build();
        AppVersionInfo client = restAdapter.create(AppVersionInfo.class);
        client.excute(BaseClient.LATEST_APP_VERSION, new Callback<VersionInfo>() {
            @Override
            public void success(VersionInfo versionInfo, Response response) {
                CacheManager.updateSpeedLogUrl(context, versionInfo.getSpeedlogurl());

                PackageInfo packageInfo = null;

                try {
                    packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (packageInfo.versionCode < Integer.parseInt(versionInfo.getVersion())) {
                    downloadAPK(context, versionInfo.getDownloadurl(), versionInfo.getMd5(), DEFAULT_VALUE);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new ClearThread(this).start();
        Intent intent = new Intent("com.ismartv.android.vod.service.keymonitor");
        server = new AsyncHttpServer();
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        try {
            nativeservice.sendMoniterKey(keyEventCode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    interface AppVersionInfo {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<VersionInfo> callback
        );
    }


    class ClearThread extends Thread {
        private Context context;

        public ClearThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                sleep(12000);
                File file = new File(context.getFilesDir(), Utilities.APP_NAME);


                if (file.exists()) {
                    file.delete();
                    if (AppConstant.DEBUG)
                        Log.d(TAG, "delete file --> " + file.getAbsolutePath());
                }
                getLatestAppVersion(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
