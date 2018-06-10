package com.gdut.dkmfromcg.commonlib.mvp.datahandler;

import com.trello.rxlifecycle2.LifecycleProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by dkmFromCG on 2018/3/27.
 * function:
 */

public class RxHandler {
    private static final String TAG = "RxHandler";
    // 订阅者集合
    private Set<Object> subscribers;

    /**
     * 注册 DataBusSubscriber
     *
     * @param subscriber 观察者,传入Presenter...当Model层返回数据时,Presenter作为观察者执行操作
     */
    public synchronized void register(Object subscriber) {
        subscribers.add(subscriber);
    }


    /**
     * 注销 DataBusSubscriber
     *
     * @param subscriber 传入Presenter
     */
    public synchronized void unRegister(Object subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * 单例模式
     */
    private static volatile RxHandler instance;

    private RxHandler() {
        subscribers = new CopyOnWriteArraySet<>();
    }

    public static synchronized RxHandler getInstance() {

        if (instance == null) {
            synchronized (RxHandler.class) {
                if (instance == null) {
                    instance = new RxHandler();
                }

            }
        }
        return instance;
    }

    private final int DEFAULT_MESSAGE = -1 << 5;
    private int messageInt = DEFAULT_MESSAGE;

    /**
     * 包装处理过程
     *
     * @param func
     */
    @SuppressWarnings("unchecked")
    public void chainProcess(LifecycleProvider lifecycleProvider, int message, Function func) {
        messageInt = message;
        Observable.just("")
                .subscribeOn(Schedulers.io()) // 指定处理过程在 IO 线程
                .map(func)   // 包装处理过程
                .compose(lifecycleProvider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())  // 指定事件消费在 Main 线程
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object data) throws Exception {
                        if (data == null) {
                            return;
                        }
                        send(data);
                    }
                });
    }


    /**
     * 发送数据
     *
     * @param data
     */
    private void send(Object data) {
        for (Object subscriber : subscribers) {
            // 扫描注解，将数据发送到注册的对象的标记方法
            callMethodByAnnotation(subscriber, data);
        }
    }

    /**
     * 反射获取对象方法列表，判断：
     * 1 是否被注解修饰
     * 2 参数类型是否和 data 类型一致
     *
     * @param target
     * @param data
     */

    private void callMethodByAnnotation(Object target, Object data) {

        Method[] methodArray = target.getClass().getDeclaredMethods();
        for (Method method : methodArray) {
            try {
                // 被 @HandleMessage 修饰的方法
                if (method.isAnnotationPresent(HandleMessage.class)) {
                    int message = method.getAnnotation(HandleMessage.class).message();
                    if (message == messageInt) {
                        method.invoke(target, data);
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
