package com.huaijie.tools.net.async.http;


import com.huaijie.tools.net.async.AsyncSocket;
import com.huaijie.tools.net.async.callback.CompletedCallback;
import com.huaijie.tools.net.async.http.libcore.ResponseHeaders;

public interface AsyncHttpResponse extends AsyncSocket {
    public void setEndCallback(CompletedCallback handler);
    public CompletedCallback getEndCallback();
    public ResponseHeaders getHeaders();
    public void end();
    public AsyncSocket detachSocket();
    public AsyncHttpRequest getRequest();
}
