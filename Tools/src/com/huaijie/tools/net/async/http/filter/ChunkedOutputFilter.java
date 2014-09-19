package com.huaijie.tools.net.async.http.filter;


import com.huaijie.tools.net.async.ByteBufferList;
import com.huaijie.tools.net.async.DataSink;
import com.huaijie.tools.net.async.FilteredDataSink;

import java.nio.ByteBuffer;

public class ChunkedOutputFilter extends FilteredDataSink {
    public ChunkedOutputFilter(DataSink sink) {
        super(sink);
    }

    @Override
    public ByteBufferList filter(ByteBufferList bb) {
        String chunkLen = Integer.toString(bb.remaining(), 16) + "\r\n";
        bb.addFirst(ByteBuffer.wrap(chunkLen.getBytes()));
        bb.add(ByteBuffer.wrap("\r\n".getBytes()));
        return bb;
    }
}
