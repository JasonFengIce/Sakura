package com.huaijie.tools.net.async.http.server;


import com.huaijie.tools.net.async.AsyncSocket;
import com.huaijie.tools.net.async.DataEmitter;
import com.huaijie.tools.net.async.http.Multimap;
import com.huaijie.tools.net.async.http.body.AsyncHttpRequestBody;
import com.huaijie.tools.net.async.http.libcore.RequestHeaders;

import java.util.regex.Matcher;

public interface AsyncHttpServerRequest extends DataEmitter
{
    public RequestHeaders getHeaders();
    public Matcher getMatcher();
    public AsyncHttpRequestBody getBody();
    public AsyncSocket getSocket();
    public String getPath();
    public Multimap getQuery();
    public String getMethod();
}
