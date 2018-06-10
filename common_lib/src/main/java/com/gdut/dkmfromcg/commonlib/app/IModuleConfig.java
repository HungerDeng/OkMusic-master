package com.gdut.dkmfromcg.commonlib.app;

import android.app.Application;
import android.content.Context;

import java.util.List;


public interface IModuleConfig {

    void injectAppLifecycle(Context context, List<IAppLife> appLifeList);

    void injectActivityLifecycle(Context context, List<Application.ActivityLifecycleCallbacks> lifecycleCallbacks);

}
