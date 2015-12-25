package cn.ismartv.iris.core.client;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huaijie on 10/30/15.
 */
public class BaseClient extends Thread {
    private static final String TAG = "IsmartvUrlClient";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int CONNECT_TIME_OUT = 10;

    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;

    private static final int FAILURE_4XX = 0x0004;
    private static final int FAILURE_5XX = 0x0005;

    private String mUrl;
    private String mParams;
    private CallBack mCallback;
    private Method mMethod;


    @Override
    public void run() {
        switch (mMethod) {
            case GET:
                doGet();
                break;
            case POST:
                doPost();
                break;
        }
    }


    public interface CallBack {
        void onSuccess(String result);

        void onFailed(String error);
    }


    public void doRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.mParams = stringBuffer.toString();
        this.mUrl = api;
        this.mCallback = callback;
        this.mMethod = method;
        start();
    }

    public void doRequest(String api, CallBack callback) {
        this.mParams = "";
        this.mUrl = api;
        this.mCallback = callback;
        this.mMethod = Method.GET;
        start();
    }


    public enum Method {
        GET,
        POST
    }

    private void doGet() {
        try {
            String api = mUrl + "?" + mParams;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(api)
                    .build();
            Response response = client.newCall(request).execute();
            handleResponse(response);
        } catch (IOException e) {
            Message message = messageHandler.obtainMessage();
            message.what = FAILURE;
            message.obj = e.getMessage();
            messageHandler.sendMessage(message);
        }

    }

    private void doPost() {
        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
            RequestBody body = RequestBody.create(JSON, mParams);
            Request request = new Request.Builder()
                    .url(mUrl)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            handleResponse(response);
        } catch (IOException e) {
            Message message = messageHandler.obtainMessage();
            message.what = FAILURE;
            message.obj = e.getMessage();
            messageHandler.sendMessage(message);
            e.printStackTrace();
        }
    }


    private void handleResponse(Response response) {
        Message message = messageHandler.obtainMessage();
        if (response.code() >= 100 && response.code() < 400) {
            message.what = SUCCESS;
        } else if (response.code() >= 400 && response.code() < 500) {
            message.what = FAILURE_4XX;
        } else if (response.code() >= 500 && response.code() < 600) {
            message.what = FAILURE_5XX;
        }
        String responseResult = response.body().toString();
        message.obj = responseResult;
        messageHandler.sendMessage(message);

        String logMsg = "---> BEGIN\n" +
                "\t<--- Request URL: " + "\t" + mUrl + "\n" +
                "\t<--- Request Method: " + "\t" + mMethod.toString() + "\n" +
                "\t<--- Request Params: " + "\t" + mParams + "\n" +
                "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                "\t<--- Response Result: " + "\t" + responseResult + "\n" +
                "\t---> END";

        switch (message.what) {
            case FAILURE_4XX:
            case FAILURE_5XX:
                Log.e(TAG, logMsg);
                break;
            case SUCCESS:
                Log.i(TAG, logMsg);
                break;
        }
    }


    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    mCallback.onSuccess((String) msg.obj);
                    break;
                case FAILURE:
                case FAILURE_4XX:
                case FAILURE_5XX:
                    mCallback.onFailed((String) msg.obj);
                    break;
            }
        }
    };
}
