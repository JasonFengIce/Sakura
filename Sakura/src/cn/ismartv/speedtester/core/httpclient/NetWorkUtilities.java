package cn.ismartv.speedtester.core.httpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.data.*;
import cn.ismartv.speedtester.ui.fragment.FeedbackFragment;
import cn.ismartv.speedtester.ui.fragment.NodeFragment;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.Utilities;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by fenghb on 14-7-4.
 */
public class NetWorkUtilities {
    public static final String TAG = "NetWorkUtilities";


    public static void uploadFeedback(Context context, FeedBack feedBack) {

        final String str = new Gson().toJson(feedBack);
        Log.d(TAG, str);
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "upload feedback is running...");
                String localeName = Locale.getDefault().toString();
                URL url;
                HttpURLConnection conn = null;
                InputStream inputStream;
                BufferedReader reader = null;
                StringBuffer sb = new StringBuffer();
                OutputStream outputStream;
                BufferedWriter writer = null;
                try {
                    url = new URL("http://iris.tvxio.com/customer/pointlogs/");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("content-type", "text/json");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", android.os.Build.MODEL.replaceAll(" ", "_") + "/" + android.os.Build.ID + " " + DevicesUtilities.getSNCode());
                    Log.d(TAG, "RequestProperty : " + conn.getRequestProperty("User-Agent"));
                    conn.setRequestProperty("Accept-Language", localeName);
                    Log.d("request url : ", url.toString());
                    Log.d("requestHeader q", str);
                    outputStream = conn.getOutputStream();
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write("q=" + str);
                    writer.flush();
                    int statusCode = conn.getResponseCode();
                    Log.d("statusCode", "" + statusCode);
                    if (statusCode == 200) {
                        inputStream = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if ("OK".equals(sb.toString()) && null != FeedbackFragment.messageHandler) {
                    FeedbackFragment.messageHandler.sendEmptyMessage(FeedbackFragment.UPLAOD_FEEDBACK_COMPLETE);
                } else {
                    FeedbackFragment.messageHandler.sendEmptyMessage(FeedbackFragment.UPLAOD_FEEDBACK_FAILED);
                }
                Log.d(TAG, "upload feedback is end...");
            }
        }.start();
    }
}
