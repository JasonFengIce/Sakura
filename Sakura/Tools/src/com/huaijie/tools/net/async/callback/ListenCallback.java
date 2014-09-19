package com.huaijie.tools.net.async.callback;


import com.huaijie.tools.net.async.AsyncServerSocket;
import com.huaijie.tools.net.async.AsyncSocket;

public interface ListenCallback extends CompletedCallback {
    public void onAccepted(AsyncSocket socket);
    public void onListening(AsyncServerSocket socket);
}
