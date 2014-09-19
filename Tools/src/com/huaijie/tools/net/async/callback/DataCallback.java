package com.huaijie.tools.net.async.callback;


import com.huaijie.tools.net.async.ByteBufferList;
import com.huaijie.tools.net.async.DataEmitter;

public interface DataCallback {
    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb);
}
