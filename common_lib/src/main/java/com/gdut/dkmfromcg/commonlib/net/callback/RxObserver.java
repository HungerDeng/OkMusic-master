package com.gdut.dkmfromcg.commonlib.net.callback;

import android.text.TextUtils;

import com.gdut.dkmfromcg.commonlib.net.IHttpRequestListener;
import com.gdut.dkmfromcg.commonlib.net.RxActionManagerImpl;
import com.gdut.dkmfromcg.commonlib.net.exception.ApiException;
import com.gdut.dkmfromcg.commonlib.net.exception.ExceptionEngine;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 适用Retrofit网络请求Observer(监听者)
 * 备注:
 * 1.重写onSubscribe，添加请求标识
 * 2.重写onError，封装错误/异常处理，移除请求
 * 3.重写onNext，移除请求
 * 4.重写cancel，取消请求
 *
 * @author ZhongDaFeng
 */
public abstract class RxObserver<T> implements Observer<T>,IHttpRequestListener{

    private String mTag;//请求标识

    public RxObserver() {
    }

    public RxObserver(String tag) {
        this.mTag = tag;
    }

    protected abstract void onStart(Disposable d);
    protected abstract void onSuccess(T response);
    protected abstract void onError(ApiException e);


    @Override
    public void onSubscribe(Disposable d) {
        if (!TextUtils.isEmpty(mTag)) {
            RxActionManagerImpl.getInstance().add(mTag, d);
        }
        onStart(d);
    }

    @Override
    public void onNext(T t) {
        if (!TextUtils.isEmpty(mTag)) {
            RxActionManagerImpl.getInstance().remove(mTag);
        }
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        RxActionManagerImpl.getInstance().remove(mTag);
        if (e instanceof ApiException) {
            onError((ApiException) e);
        } else {
            onError(new ApiException(e, ExceptionEngine.UN_KNOWN_ERROR));
        }
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void cancel() {
        if (!TextUtils.isEmpty(mTag)) {
            RxActionManagerImpl.getInstance().cancel(mTag);
        }
    }

    /**
     * 是否已经处理
     *
     * @author ZhongDaFeng
     */
    public boolean isDisposed() {
        if (TextUtils.isEmpty(mTag)) {
            return true;
        }
        return RxActionManagerImpl.getInstance().isDisposed(mTag);
    }
}
