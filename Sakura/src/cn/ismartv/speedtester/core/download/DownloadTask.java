package cn.ismartv.speedtester.core.download;

import android.content.Context;
import android.database.Cursor;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.utils.DeviceUtils;

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
    private List<Map<String, String>> nodes;
    public volatile boolean running = false;
    private OnSpeedTestListener listener;


    public interface OnSpeedTestListener {
        public void changeStatus(String id, String cdnId, boolean status);

        public void compelte(String id, String cdnId, int speed);

        public void allCompelte();
    }

    public void setSpeedTestListener(OnSpeedTestListener listener) {
        this.listener = listener;
    }

    public DownloadTask(Context context, Cursor cursor) {
        this.context = context;
        nodes = new ArrayList<Map<String, String>>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(NodeCacheTable.CDN_ID, cursor.getString(cursor.getColumnIndex(NodeCacheTable.CDN_ID)));
            map.put(NodeCacheTable.URL, cursor.getString(cursor.getColumnIndex(NodeCacheTable.URL)));
            map.put(NodeCacheTable.ID, cursor.getString(cursor.getColumnIndex(NodeCacheTable.ID)));
            nodes.add(map);
        }
    }


    @Override
    public void run() {
        running = true;
        for (Map<String, String> map : nodes) {
            if (running) {
                Timer timer = new Timer();
                timer.start();
                int bytesum = 0;
                int byteread;
                String cdnId = "0";
                String recordId = "0";
                File fileName;
                URL url;
                try {
                    long startTime = System.currentTimeMillis();
                    url = new URL(map.get(NodeCacheTable.URL));
                    cdnId = map.get(NodeCacheTable.CDN_ID);
                    recordId = map.get(NodeCacheTable.ID);
                    listener.changeStatus(recordId, cdnId, true);
                    fileName = new File(DeviceUtils.getAppCacheDirectory(context), url.getHost() + SUFFIX);
                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(5000);
                    InputStream inStream = conn.getInputStream();
                    FileOutputStream fs = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    while ((byteread = inStream.read(buffer)) != -1 && timer.timer < TIME_OVER) {
                        bytesum += byteread;
                        fs.write(buffer, 0, byteread);
                    }
                    long stopTime = System.currentTimeMillis();
                    int speed = getKBperSECOND(bytesum, startTime, stopTime);
                    //update node cache
//                    CacheManager.updateNodeCache(context, cdnId, speed);
                    listener.compelte(recordId, cdnId, speed);
                    listener.changeStatus(recordId, cdnId, false);
//                    CacheManager.updateRunning(context, cndId, "false");
//
//                    uploadTestResult(cndId, speed);
                    fs.flush();
                    fs.close();
                    inStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    running = false;
                } catch (IOException e) {
                    listener.compelte(recordId, cdnId, -1);
                    listener.changeStatus(recordId, cdnId, false);
                    running = false;
                }

            }
        }
        listener.allCompelte();
        running = false;
    }

    private final int getKBperSECOND(long dataByte, long start, long stop) {
        return (int) (((float) dataByte) / ((float) (stop - start)) * (1024f / 1000f));
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

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

}