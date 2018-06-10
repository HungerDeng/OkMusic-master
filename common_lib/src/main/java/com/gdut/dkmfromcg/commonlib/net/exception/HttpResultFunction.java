package com.gdut.dkmfromcg.commonlib.net.exception;

import android.support.annotation.NonNull;


import com.gdut.dkmfromcg.commonlib.util.log.Logger;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


/**
 * Created by dkmFromCG on 2018/4/7.
 * function:
 */

public class HttpResultFunction<T> implements Function<Throwable, Observable<T>> {

    @Override
    public Observable<T> apply(@NonNull Throwable throwable) throws Exception {
        //打印具体错误
        Logger.e("HttpResultFunction:" , String.valueOf(throwable));
        return Observable.error(ExceptionEngine.handleException(throwable));
    }
}
