package com.huaijie.tools.net.async.wrapper;


import com.huaijie.tools.net.async.AsyncSocket;

public interface AsyncSocketWrapper extends AsyncSocket, DataEmitterWrapper {
    public AsyncSocket getSocket();
}
