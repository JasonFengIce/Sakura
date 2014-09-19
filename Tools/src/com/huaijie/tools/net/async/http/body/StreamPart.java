package com.huaijie.tools.net.async.http.body;

import com.huaijie.tools.net.async.DataSink;
import com.huaijie.tools.net.async.Util;
import com.huaijie.tools.net.async.callback.CompletedCallback;
import org.apache.http.NameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class StreamPart extends Part {
    public StreamPart(String name, long length, List<NameValuePair> contentDisposition) {
        super(name, length, contentDisposition);
    }

    @Override
    public void write(DataSink sink, CompletedCallback callback) {
        try {
            InputStream is = getInputStream();
            Util.pump(is, sink, callback);
        } catch (Exception e) {
            callback.onCompleted(e);
        }
    }

    protected abstract InputStream getInputStream() throws IOException;
}
