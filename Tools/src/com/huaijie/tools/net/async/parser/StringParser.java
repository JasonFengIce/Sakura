package com.huaijie.tools.net.async.parser;


import com.huaijie.tools.net.async.ByteBufferList;
import com.huaijie.tools.net.async.DataEmitter;
import com.huaijie.tools.net.async.DataSink;
import com.huaijie.tools.net.async.callback.CompletedCallback;
import com.huaijie.tools.net.async.future.Future;
import com.huaijie.tools.net.async.future.TransformFuture;

import java.nio.charset.Charset;

/**
 * Created by koush on 5/27/13.
 */
public class StringParser implements AsyncParser<String> {
    @Override
    public Future<String> parse(DataEmitter emitter) {
        final String charset = emitter.charset();
        return new ByteBufferListParser().parse(emitter)
                .then(new TransformFuture<String, ByteBufferList>() {
                    @Override
                    protected void transform(ByteBufferList result) throws Exception {
                        setComplete(result.readString(charset != null ? Charset.forName(charset) : null));
                    }
                });
    }

    @Override
    public void write(DataSink sink, String value, CompletedCallback completed) {
        new ByteBufferListParser().write(sink, new ByteBufferList(value.getBytes()), completed);
    }
}
