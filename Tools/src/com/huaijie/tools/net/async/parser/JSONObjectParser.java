package com.huaijie.tools.net.async.parser;

import com.huaijie.tools.net.async.DataEmitter;
import com.huaijie.tools.net.async.DataSink;
import com.huaijie.tools.net.async.callback.CompletedCallback;
import com.huaijie.tools.net.async.future.Future;
import com.huaijie.tools.net.async.future.TransformFuture;
import org.json.JSONObject;

/**
 * Created by koush on 5/27/13.
 */
public class JSONObjectParser implements AsyncParser<JSONObject> {
    @Override
    public Future<JSONObject> parse(DataEmitter emitter) {
        return new StringParser().parse(emitter)
        .then(new TransformFuture<JSONObject, String>() {
            @Override
            protected void transform(String result) throws Exception {
                setComplete(new JSONObject(result));
            }
        });
    }

    @Override
    public void write(DataSink sink, JSONObject value, CompletedCallback completed) {
        new StringParser().write(sink, value.toString(), completed);
    }
}
