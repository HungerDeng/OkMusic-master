package com.gdut.dkmfromcg.commonlib.net;

import io.reactivex.disposables.Disposable;

/**
 * Created by dkmFromCG on 2018/4/8.
 * function:
 */

public interface IRxActionManager<T> {

    /**
     * 添加
     *
     * @param tag
     * @param disposable
     */
    void add(T tag, Disposable disposable);

    /**
     * 移除
     *
     * @param tag
     */
    void remove(T tag);

    /**
     * 取消
     *
     * @param tag
     */
    void cancel(T tag);

}
