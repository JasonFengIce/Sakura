package com.huaijie.tools.net.async.wrapper;


import com.huaijie.tools.net.async.DataEmitter;

public interface DataEmitterWrapper extends DataEmitter {
    public DataEmitter getDataEmitter();
}
