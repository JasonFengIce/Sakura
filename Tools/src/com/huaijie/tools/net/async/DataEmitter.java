package com.huaijie.tools.net.async;

import com.huaijie.tools.net.async.callback.CompletedCallback;
import com.huaijie.tools.net.async.callback.DataCallback;

public interface DataEmitter {
    public void setDataCallback(DataCallback callback);

    public DataCallback getDataCallback();

    public boolean isChunked();

    public void pause();

    public void resume();

    public void close();

    public boolean isPaused();

    public void setEndCallback(CompletedCallback callback);

    public CompletedCallback getEndCallback();

    public AsyncServer getServer();

    public String charset();
}
