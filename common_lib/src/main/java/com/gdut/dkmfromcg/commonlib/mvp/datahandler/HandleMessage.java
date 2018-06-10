package com.gdut.dkmfromcg.commonlib.mvp.datahandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by dkmFromCG on 2018/3/27.
 * function:自定义注解，用于标记观察者(Presenter)的方法
 * 当 Model 数据完成时:
 * 相当于在Model层,使用Handler发送一条Message;然后在Presenter层使用该注解,相当于接收Message,并执行Handler.handleMessage
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleMessage {
    int message() default -1<<2;
}
