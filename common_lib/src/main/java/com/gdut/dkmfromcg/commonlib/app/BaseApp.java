package com.gdut.dkmfromcg.commonlib.app;

import android.app.Application;
import android.content.Context;

import com.gdut.dkmfromcg.commonlib.util.config.Configs;
import com.gdut.dkmfromcg.commonlib.util.log.Logger;


/**
 * Created by dkmFromCG on 2018/4/25.
 * function:
 */

public class BaseApp extends Application {

    private static final String TAG = "BaseApp";

    private ApplicationDelegate mApplicationDelegate = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Logger.d(TAG,"attachBaseContext");
        mApplicationDelegate = new ApplicationDelegate();
        mApplicationDelegate.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        Logger.d(TAG,"onCreate");
        super.onCreate();
        mApplicationDelegate = new ApplicationDelegate();
        mApplicationDelegate.attachBaseContext(this);
        //初识化全局变量
        Configs.init(this)
                .putApiHost("https://www.easy-mock.com/")
                .withWeChatAppId("your wechat appId")
                .withWeChatAppSecret("your wechat appSecret")
                .configure();
        /*Iconify.with(new FontAwesomeModule())
                .with(new FontEcModule());
        ARouter.openLog();
        ARouter.openDebug();
        ARouter.init(this);*/
        if (mApplicationDelegate != null) {
            mApplicationDelegate.onCreate(this);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mApplicationDelegate != null) {
            mApplicationDelegate.onTerminate(this);
        }
    }
}
