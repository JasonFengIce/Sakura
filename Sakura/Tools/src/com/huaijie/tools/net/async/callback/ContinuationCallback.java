package com.huaijie.tools.net.async.callback;


import com.huaijie.tools.net.async.future.Continuation;

public interface ContinuationCallback {
    public void onContinue(Continuation continuation, CompletedCallback next) throws Exception;
}
