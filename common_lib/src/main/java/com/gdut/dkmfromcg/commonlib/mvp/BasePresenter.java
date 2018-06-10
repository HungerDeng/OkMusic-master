package com.gdut.dkmfromcg.commonlib.mvp;


import com.gdut.dkmfromcg.commonlib.mvp.datahandler.RxHandler;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by dkmFromCG on 2018/3/27.
 * function: 当Presenter请求Model完成后,避免了View层已经为null的情况,
 * 采用动态代理的方式,对View判空
 */

public class BasePresenter<V extends IView> {

    /**
     * 弱引用, 防止内存泄漏
     */
    private WeakReference<V> mViewWeakReference;
    private V mProxyView;

    /**
     * 关联V层和P层
     */
    @SuppressWarnings("unchecked")
    private void attachView(V v) {
        mViewWeakReference = new WeakReference<>(v);
        MvpViewHandler viewHandler = new MvpViewHandler(mViewWeakReference.get());
        mProxyView = (V) Proxy.newProxyInstance(v.getClass().getClassLoader(), v.getClass().getInterfaces(), viewHandler);
        RxHandler.getInstance().register(this);
    }

    /**
     * @return P层和V层是否关联.
     */
    private boolean isViewAttached() {
        return mViewWeakReference != null && mViewWeakReference.get() != null;
    }


    private class MvpViewHandler implements InvocationHandler {
        private IView mvpView;

        MvpViewHandler(IView mvpView) {
            this.mvpView = mvpView;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果V层没被销毁, 执行V层的方法.
            if (isViewAttached()) {
                return method.invoke(mvpView, args);
            }
            //P层不需要关注V层的返回值
            return null;
        }
    }

    public BasePresenter(V view) {
        attachView(view);
    }

    public V getView() {
        return mProxyView;
    }

    /**
     * 断开V层和P层
     */
    public void detachView() {
        if (isViewAttached()) {
            mViewWeakReference.clear();
            mViewWeakReference = null;
        }
        RxHandler.getInstance().unRegister(this);
    }

}
