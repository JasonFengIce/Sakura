package cn.ismartv.iris.core.client;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huaijie on 5/28/15.
 */
public class IsmartvUrlClient extends Thread {
    private static final String TAG = "IsmartvClient:";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int SUCCESS = 0x0001;
    private static final int FAILURE = 0x0002;

    private static final int FAILURE_4XX = 0x0004;
    private static final int FAILURE_5XX = 0x0005;

    private String url;
    private String params;
    private CallBack callback;
    private Method method;
    private MessageHandler messageHandler;

    private static Context mContext;

    private ErrorHandler errorHandler = ErrorHandler.SEND_BROADCAST;

    public static void initializeWithContext(Context context) {
        mContext = context;
    }

    public IsmartvUrlClient() {
        messageHandler = new MessageHandler(this);
    }

    @Override
    public void run() {

        switch (method) {
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

        void onFailed(Exception exception);
    }


    static class MessageHandler extends Handler {
        WeakReference<IsmartvUrlClient> weakReference;

        public MessageHandler(IsmartvUrlClient client) {
            weakReference = new WeakReference<IsmartvUrlClient>(client);
        }

        @Override
        public void handleMessage(Message msg) {
            IsmartvUrlClient client = weakReference.get();
            if (client != null) {
                switch (msg.what) {
                    case SUCCESS:
                        client.callback.onSuccess((String) msg.obj);
                        break;
                    case FAILURE:
                        client.callback.onFailed((Exception) msg.obj);
                        switch (client.errorHandler) {
                            case LOG_MESSAGE:
                                break;
                            case SEND_BROADCAST:
                                client.sendConnectErrorBroadcast(((Exception) msg.obj).getMessage());
                                break;
                        }
                        break;
                    case FAILURE_4XX:
                        client.callback.onFailed((Exception) msg.obj);
                        break;
                    case FAILURE_5XX:
                        client.callback.onFailed((Exception) msg.obj);
                        switch (client.errorHandler) {
                            case LOG_MESSAGE:
                                break;
                            case SEND_BROADCAST:
                                client.sendConnectErrorBroadcast(((Exception) msg.obj).getMessage());
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public void doNormalRequest(Method method, String api, HashMap<String, String> hashMap, CallBack callback) {
        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key).append("=").append(value).append("&");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        this.params = stringBuffer.toString();
        this.url = api;
        this.callback = callback;
        this.method = method;
        start();
    }


    public enum Method {
        GET,
        POST
    }

    public enum ErrorHandler {
        LOG_MESSAGE,
        SEND_BROADCAST
    }

    private void doGet() {
        Message message = messageHandler.obtainMessage();
        try {
            String api = url + "?" + params;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(api)
                    .build();

            Response response;
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "---> BEGIN\n" +
                    "\t<--- Request URL: " + "\t" + api + "\n" +
                    "\t<--- Request Method: " + "\t" + "GET" + "\n" +
                    "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                    "\t<--- Response Result: " + "\t" + result + "\n" +
                    "\t---> END"
            );
            if (response.code() >= 400 && response.code() < 500) {
                message.what = FAILURE_4XX;
                message.obj = new IOException("网络请求客户端错误!!!");
            } else if (response.code() >= 500) {
                message.what = FAILURE_5XX;
                message.obj = new IOException("网络请求服务端错误!!!");

            } else {
                message.what = SUCCESS;
                message.obj = result;
            }
        } catch (Exception e) {
            message.what = FAILURE;
            if(e.getMessage() == null)
            	e = new Exception("请求超时");
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }

    private void doPost() {
        Message message = messageHandler.obtainMessage();
        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            RequestBody body = RequestBody.create(JSON, params);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response;
            response = client.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "---> BEGIN\n" +
                    "\t<--- Request URL: " + "\t" + url + "\n" +
                    "\t<--- Request Method: " + "\t" + "POST" + "\n" +
                    "\t<--- Request Params: " + "\t" + params + "\n" +
                    "\t<--- Response Code: " + "\t" + response.code() + "\n" +
                    "\t<--- Response Result: " + "\t" + result + "\n" +
                    "\t---> END"
            );

            if (response.code() >= 400 && response.code() < 500) {
                message.what = FAILURE_4XX;
                message.obj = new IOException("网络请求客户端错误!!!");
            } else if (response.code() >= 500) {
                message.what = FAILURE_5XX;
                message.obj = new IOException("网络请求服务端错误!!!");

            } else {
                message.what = SUCCESS;
                message.obj = result;
            }
        } catch (Exception e) {
            message.what = FAILURE;
            if(e.getMessage() == null)
            	e = new Exception("请求超时");
            message.obj = e;
        }
        messageHandler.sendMessage(message);
    }

    private void sendConnectErrorBroadcast(String msg) {
        if (mContext != null) {
            Intent intent = new Intent();
            intent.putExtra("data", msg);
//            intent.setAction(BaseActivity.ACTION_CONNECT_ERROR);
            mContext.sendBroadcast(intent);
        }
    }
}
