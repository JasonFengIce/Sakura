package com.huaijie.tools.net.async;


import com.huaijie.tools.net.async.callback.CompletedCallback;
import com.huaijie.tools.net.async.callback.WritableCallback;

import java.nio.ByteBuffer;

public interface DataSink {
    public void write(ByteBuffer bb);

    public void write(ByteBufferList bb);

    public void setWriteableCallback(WritableCallback handler);

    public WritableCallback getWriteableCallback();

    public boolean isOpen();

    public void close();

    public void end();

    public void setClosedCallback(CompletedCallback handler);

    public CompletedCallback getClosedCallback();

    public AsyncServer getServer();
}
