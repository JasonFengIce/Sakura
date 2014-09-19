package com.huaijie.tools.net.async.http.callback;


import com.huaijie.tools.net.async.callback.ResultCallback;
import com.huaijie.tools.net.async.http.AsyncHttpResponse;

public interface RequestCallback<T> extends ResultCallback<AsyncHttpResponse, T> {
    public void onConnect(AsyncHttpResponse response);
    public void onProgress(AsyncHttpResponse response, long downloaded, long total);
}
