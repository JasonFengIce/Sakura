package com.huaijie.tools.net.async.http;


import com.huaijie.tools.net.async.AsyncSocket;
import com.huaijie.tools.net.async.DataEmitter;
import com.huaijie.tools.net.async.callback.ConnectCallback;
import com.huaijie.tools.net.async.future.Cancellable;
import com.huaijie.tools.net.async.http.libcore.ResponseHeaders;
import com.huaijie.tools.net.async.util.UntypedHashtable;

public interface AsyncHttpClientMiddleware {
    public static class GetSocketData {
        public UntypedHashtable state;
        public AsyncHttpRequest request;
        public ConnectCallback connectCallback;
        public Cancellable socketCancellable;

        public GetSocketData() {
            state = new UntypedHashtable();
        }
    }

    public static class OnSocketData extends GetSocketData {
        public AsyncSocket socket;
    }

    public static class OnHeadersReceivedData extends OnSocketData {
        public ResponseHeaders headers;
    }

    public static class OnBodyData extends OnHeadersReceivedData {
        public DataEmitter bodyEmitter;
    }

    public static class OnRequestCompleteData extends OnBodyData {
        public Exception exception;
    }

    public Cancellable getSocket(GetSocketData data);

    public void onSocket(OnSocketData data);

    public void onHeadersReceived(OnHeadersReceivedData data);

    public void onBodyDecoder(OnBodyData data);

    public void onRequestComplete(OnRequestCompleteData data);
}
