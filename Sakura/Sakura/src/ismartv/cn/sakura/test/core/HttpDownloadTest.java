package ismartv.cn.sakura.test.core;

import android.test.AndroidTestCase;
import android.util.Log;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.StringUtilities;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by fenghb on 14-7-2.
 */
public class HttpDownloadTest extends AndroidTestCase {
    private static final String TAG = "HttpDownloadTest";
    private static final String FILE_URL = "http://210.14.137.56/cdn/speedtest.ts";


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static boolean httpDownload(String httpUrl, String saveFile) {
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;

        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }

        try {
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(saveFile);

            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void download() {
//        HttpDownload httpDownload = new HttpDownload();
//        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
//        Map<String, String> map = new HashMap<String, String>();
////        map.put(DownloadTask.NODE_IP, "dldir1.qq.com");
////        map.put(DownloadTask.NODE_URL, "http://dldir1.qq.com/qqfile/qq/QQ5.5/11433/QQ5.5.exe");
//        list.add(map);
    }


    public void singleDownlaod() {
        HttpClient client = new DefaultHttpClient();
        long timer = System.currentTimeMillis();

        String ip = "210.14.137.56";
        String url = "http://" + ip + "/cdn/speedtest.ts";
        if ((!StringUtilities.isEmpty(url)) && DevicesUtilities.isExistSDCard()) {
            HttpGet get = new HttpGet(url);
            Log.d(TAG, "http get url is : " + url);
            try {
                long startTime = System.currentTimeMillis();
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                long size = entity.getContentLength();
                InputStream inputStream = entity.getContent();
                //if inputStream is null
                if (null != inputStream) {
                    String filename = ip + ".ts";
                    File file = new File(DevicesUtilities.getAppCacheDirectory(), filename);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int location;
                    while ((location = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, location);
                        if (System.currentTimeMillis() - timer > 10000) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            inputStream.close();
                            long stopTime = System.currentTimeMillis();
                            Log.d(TAG, "\nfile name : " + filename + "\n" +
                                            "file size : " + size + " byte \n" +
                                            "start time : " + startTime + "\n" +
                                            "stop time : " + stopTime + "\n"
                            );
                            break;
                        }
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                    long stopTime = System.currentTimeMillis();
                    Log.d(TAG, "\nfile name : " + filename + "\n" +
                                    "file size : " + size + " byte \n" +
                                    "start time : " + startTime + "\n" +
                                    "stop time : " + stopTime + "\n"
                    );
                } else {
                    Log.e(TAG, "not get inputstream from http entity content!!!");
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "FileNotFoundException : " + e.getMessage());
            } catch (ClientProtocolException e) {
                Log.e(TAG, "ClientProtocolException : " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "url or sdcard is not available!!!");
        }
    }

    private final String getKBperSECOND(long dataByte, long start, long stop) {
        return String.valueOf(((float) dataByte) / ((float) (stop - start)) * (1024f / 1000f));
    }
}
