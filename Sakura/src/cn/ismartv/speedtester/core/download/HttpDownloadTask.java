package cn.ismartv.speedtester.core.download;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.utils.DeviceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by huaijie on 12/26/14.
 */
public class HttpDownloadTask extends AsyncTask<List<NodeCacheTable>, String, Long> {
    private static final String TAG = HttpDownloadTask.class.getSimpleName();

    /**
     * 常量声明
     */
    private static final int CONNECT_TIME_OUT = 5000;
    private static final String DEFAULT_DOWNLOAD_CACHE_NAME = "download.cache";

    /**
     *
     */
    private final File defaultCacheFile;
    private final Context mContext;

    public interface OnCompleteListener {
        /**
         * 单个节点测速完成
         */
        public void onSingleComplete();

        /**
         * 所有节点测速完成
         */
        public void onAllComplete();
    }


    public HttpDownloadTask(Context context) {
        this.mContext = context;
        defaultCacheFile = new File(DeviceUtils.getAppCacheDirectory(mContext), DEFAULT_DOWNLOAD_CACHE_NAME);
    }

    @Override
    protected Long doInBackground(List<NodeCacheTable>... params) {


        for (NodeCacheTable cacheTable : params[0]) {
            /**
             * 测速下载
             */
            if (cacheTable.setRun) {
                try {
                    /**
                     * 开始测速时间
                     */
                    long startTime = System.currentTimeMillis();

                    URL url = new URL(cacheTable.url);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(CONNECT_TIME_OUT);
                    /**
                     *获得输入流
                     */
                    InputStream inputStream = connection.getInputStream();

                    /**
                     * 创建文件输出流
                     */
                    FileOutputStream fileOutputStream = new FileOutputStream(defaultCacheFile);

                    byte[] buffer = new byte[100];
                    int byteRead = 0;
                    int byteSum = 0;
                    while ((byteRead = inputStream.read(buffer)) != -1) {
                        byteSum += byteRead;
                        fileOutputStream.write(buffer, 0, byteRead);
                    }

                    /**
                     * 结束测速时间
                     */
                    long stopTime = System.currentTimeMillis();


                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();

                } catch (MalformedURLException e) {
                    if (AppConstant.DEBUG) {
                        e.printStackTrace();
                    } else {
                        Log.e(TAG, "MalformedURLException ---> " + e.getMessage());
                    }
                } catch (IOException e) {
                    if (AppConstant.DEBUG) {
                        e.printStackTrace();
                    } else {
                        Log.e(TAG, "IOException ---> " + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Long aLong) {
        super.onCancelled(aLong);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    /**
     * 计算测试速度
     *
     * @param dataByte
     * @param startTime
     * @param stopTime
     * @return 返回单位为 KB / S
     */
    private final int calculateSpeed(long dataByte, long startTime, long stopTime) {
        return (int) (((float) dataByte) / ((float) (stopTime - startTime)) * (1024f / 1000f));
    }

    /**
     * 计时器
     */
    private class Timer extends Thread {
        private static final int TIME_OVER = 4;
        private long timer = 0;

        @Override
        public void run() {
            while (timer <= TIME_OVER) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    if (AppConstant.DEBUG)
                        e.printStackTrace();
                    else
                        Log.e(TAG, e.getMessage());
                }
                timer += 1;
            }
        }
    }
}
