package com.huaijie.tools.net.async.http;


import com.huaijie.tools.net.async.AsyncSocket;

public interface WebSocket extends AsyncSocket {
    static public interface StringCallback {
        public void onStringAvailable(String s);
    }

    public void send(byte[] bytes);

    public void send(String string);

    public void send(byte[] bytes, int offset, int len);

    public void setStringCallback(StringCallback callback);

    public StringCallback getStringCallback();

    public boolean isBuffering();

    public AsyncSocket getSocket();
}
