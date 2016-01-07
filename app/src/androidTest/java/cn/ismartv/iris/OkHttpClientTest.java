package cn.ismartv.iris;

import android.test.AndroidTestCase;
import android.util.Log;

import java.io.IOException;

import cn.ismartv.iris.core.client.OkHttpClientManager;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by huaijie on 1/7/16.
 */
public class OkHttpClientTest extends AndroidTestCase {

    private static final String TAG = "OkHttpClientTest ";

    public void testConnectTimeOut() {
        OkHttpClient client = new OkHttpClient();
        int timeout = client.connectTimeoutMillis();
        Log.i(TAG, "time out is : " + timeout);
    }

    public void testUrl() {
        OkHttpClient client = OkHttpClientManager.getClient();
        final Request request = new Request.Builder().url("http://www.baidu.com").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Response resp = response;
                Log.i(TAG, resp.body().string());
            }
        });
    }
}
