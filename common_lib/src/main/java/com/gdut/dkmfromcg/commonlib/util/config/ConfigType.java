package com.gdut.dkmfromcg.commonlib.util.config;

/**
 * Created by dkmFromCG on 2018/3/8.
 * function:
 */

public enum  ConfigType {
    API_HOST, //网络请求的域名
    APPLICATION_CONTEXT,
    CONFIG_READY,//控制或配置初识化完成与否
    ICON,
    INTERCEPTOR,//OkHttp拦截器
    ACTIVITY,
    HANDLER,

    /*WeChat 参数appId和appSecret*/
    WE_CHAT_APP_ID,
    WE_CHAT_APP_SECRET


}
