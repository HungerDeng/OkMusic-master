package com.gdut.dkmfromcg.commonlib.mvp;

import android.support.annotation.NonNull;

import com.trello.rxlifecycle2.LifecycleProvider;

import java.lang.ref.WeakReference;

/**
 * Created by dkmFromCG on 2018/4/9.
 * function:
 */

public class BaseModel {

    /**
     * 弱引用, 防止内存泄漏
     */
    private WeakReference<LifecycleProvider> mLifecycleProviderWR;
    private LifecycleProvider mLifecycleProvider;

    /**
     * 关联 LifecycleProvider 和 Model层
     *
     * @param lifecycleProvider
     */
    @SuppressWarnings("unchecked")
    private void attachLifecycleProvider(LifecycleProvider lifecycleProvider) {
        mLifecycleProviderWR = new WeakReference<>(lifecycleProvider);
        mLifecycleProvider = mLifecycleProviderWR.get();

    }

    private boolean isLifecycleProviderAttached() {
        return mLifecycleProviderWR != null && mLifecycleProviderWR.get() != null;
    }

    public BaseModel(@NonNull LifecycleProvider lifecycleProvider) {
        attachLifecycleProvider(lifecycleProvider);
    }

    public LifecycleProvider getLifecycleProvider() {
        return mLifecycleProvider;
    }


    /**
     * 断开 LifecycleProvider 和 Model层的连接
     */
    public void detachLifecycleProvider() {
        if (isLifecycleProviderAttached()) {
            mLifecycleProviderWR.clear();
            mLifecycleProviderWR = null;
        }

    }

}
