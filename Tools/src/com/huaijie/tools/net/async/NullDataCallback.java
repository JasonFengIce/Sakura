package com.huaijie.tools.net.async;


import com.huaijie.tools.net.async.callback.DataCallback;

public class NullDataCallback implements DataCallback {
    @Override
    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
        bb.recycle();
    }
}
