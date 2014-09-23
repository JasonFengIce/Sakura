package cn.ismartv.speedtester.core.download;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import cn.ismartv.speedtester.core.Message;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.httpclient.BaseClient;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.provider.NodeCache;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fenghb on 14-7-2.
 */
public class DownloadTask extends Thread {
    private static final String TAG = "DownloadTask";
    private static final String SUFFIX = ".ismartv";

    private static final int TIME_OVER = 4;

    private Context context;
    private Cursor cursor;

    private List<Map<String, String>> nodes;
    private volatile boolean running = true;

    public DownloadTask(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        nodes = new ArrayList<Map<String, String>>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("cdn_id", cursor.getString(cursor.getColumnIndex(NodeCache.CDN_ID)));
            map.put("url", cursor.getString(cursor.getColumnIndex(NodeCache.URL)));
            nodes.add(map);
        }
    }

    public static void uploadTestResult(String cdnId, String speed) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(BaseClient.HOST)
                .build();
        UploadResult client = restAdapter.create(UploadResult.class);
        client.excute("submitTestData", DevicesUtilities.getSNCode(), cdnId, speed, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    // calculate download speed

    @Override
    public void run() {
        for (Map<String, String> map : nodes) {
            if (running) {
                Log.d(TAG, "call is running......");
                Timer timer = new Timer();
                timer.start();
                int bytesum = 0;
                int byteread;
                String cndId = "";
                File fileName;
                URL url;
                try {
                    long startTime = System.currentTimeMillis();
                    url = new URL(map.get("url"));
                    cndId = map.get("cdn_id");
                    CacheManager.updateRunning(context, cndId, "true");
                    Log.d(TAG, "url is : " + url + " | cdn id is : " + cndId);
                    fileName = new File(DevicesUtilities.getAppCacheDirectory(context), url.getHost() + SUFFIX);
                    URLConnection conn = url.openConnection();
                    //url connect timeout is 5 second
                    conn.setConnectTimeout(5000);
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1 && timer.timer < TIME_OVER) {
                        bytesum += byteread;
//                Log.d(TAG, getSize(bytesum) + " time : " + timer);
                        fs.write(buffer, 0, byteread);
                    }
                    long stopTime = System.currentTimeMillis();
                    String speed = getKBperSECOND(bytesum, startTime, stopTime);
                    Log.d(TAG, "download size is : " + getSize(bytesum) + " speed is : " + speed);
                    //update node cache
                    CacheManager.updateNodeCache(context, cndId, speed);
                    CacheManager.updateRunning(context, cndId, "false");
                    uploadTestResult(cndId, speed);
                    fs.flush();
                    fs.close();
                    inStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                    e.printStackTrace();
                } catch (IOException e) {
                    CacheManager.updateNodeCache(context, cndId, "-1");
                    CacheManager.updateRunning(context, cndId, "false");
                    Message.sendMessage(context, Message.COMPLETE);
//                    HomeActivity.messageHandler.sendEmptyMessage(HomeActivity.NET_EXCEPTION);
                    Log.d(TAG, "speed test --> " + e.getMessage());
                }

            }
        }
        Log.d(TAG, "download complete!!!");
        Message.sendMessage(context, Message.COMPLETE);
    }

    private final String getKBperSECOND(long dataByte, long start, long stop) {
        return String.valueOf(((float) dataByte) / ((float) (stop - start)) * (1024f / 1000f));
    }

    private final String getSize(long data) {
        if (data < 1024)
            return data + " byte";
        else if (1024 <= data && data < 1024 * 1024)
            return ((float) data / 1024) + " kb";
        else
            return ((float) data / 1024 / 1024) + " mb";
    }


    interface UploadResult {
        @FormUrlEncoded
        @POST("/shipinkefu/getCdninfo")
        void excute(
                @Field("actiontype") String actionType,
                @Field("snCode") String snCode,
                @Field("nodeId") String nodeId,
                @Field("nodeSpeed") String nodeSpeed,
                Callback<Empty> callback
        );
    }

    class Timer extends Thread {
        private long timer;

        @Override
        public void run() {
            while (timer <= TIME_OVER) {
                try {
                    this.sleep(1000);
                    timer = timer + 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}