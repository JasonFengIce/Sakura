package com.huaijie.tools.net.async.http.callback;


import com.huaijie.tools.net.async.http.AsyncHttpResponse;

public interface HttpConnectCallback {
    public void onConnectCompleted(Exception ex, AsyncHttpResponse response);
}
