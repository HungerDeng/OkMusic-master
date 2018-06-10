package com.gdut.dkmfromcg.commonlib.net.callback;

import com.gdut.dkmfromcg.commonlib.net.exception.ApiException;

/**
 * Created by dkmFromCG on 2018/3/29.
 * function:
 */

public abstract class RequestCallback<T> {


    public abstract void onSuccess(T response);

    public abstract void onError(ApiException e);

    //IRequest
    public void onRequestStart(){}
    public void onRequestEnd(){}

    //IProgress
    public void onProgress(float progress, long transfered, long total) {}
    public void onProgress(float progress, long speed, long transfered, long total) {}

}
