package com.huaijie.tools.net.async.callback;


import com.huaijie.tools.net.async.AsyncSocket;

public interface ConnectCallback {
    public void onConnectCompleted(Exception ex, AsyncSocket socket);
}
