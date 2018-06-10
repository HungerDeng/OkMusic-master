package com.gdut.dkmfromcg.commonlib.app;

import android.app.Application;
import android.content.Context;


import com.gdut.dkmfromcg.commonlib.util.log.Logger;
import com.gdut.dkmfromcg.commonlib.util.manifest.ManifestParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkmFromCG on 2018/4/25.
 * function:
 */

public class ApplicationDelegate implements IAppLife {
    private static final String TAG = "ApplicationDelegate";

    private final List<IAppLife> appLives;
    private final List<Application.ActivityLifecycleCallbacks> lifecycleCallbacks;

    ApplicationDelegate() {
        appLives = new ArrayList<>();
        lifecycleCallbacks = new ArrayList<>();
    }

    @Override
    public void attachBaseContext(Context base) {
        Logger.d(TAG,"attachBaseContext");
        ManifestParser manifestParser = new ManifestParser(base);
        final List<IModuleConfig> list = manifestParser.parse();
        Logger.d(TAG, "IModuleConfig list.size() = " + list.size());
        if (list != null && list.size() > 0) {
            for (IModuleConfig configModule :
                    list) {
                configModule.injectAppLifecycle(base, appLives);
                configModule.injectActivityLifecycle(base, lifecycleCallbacks);
                Logger.d(TAG, configModule.getClass().getName());
            }
        }
        if (appLives.size() > 0) {
            for (IAppLife life :
                    appLives) {
                life.attachBaseContext(base);
            }
        }
    }

    @Override
    public void onCreate(Application application) {
        Logger.d(TAG, "onCreate");
        if (appLives.size() > 0) {
            for (IAppLife life :
                    appLives) {
                Logger.d(TAG,life.getClass().getName());
                life.onCreate(application);
            }
        }
        if (lifecycleCallbacks.size() > 0) {
            for (Application.ActivityLifecycleCallbacks life :
                    lifecycleCallbacks) {
                application.registerActivityLifecycleCallbacks(life);
            }
        }
    }

    @Override
    public void onTerminate(Application application) {
        Logger.d(TAG, "onTerminate");
        if (appLives.size() > 0) {
            for (IAppLife life :
                    appLives) {
                life.onTerminate(application);
            }
        }
        if (lifecycleCallbacks.size() > 0) {
            for (Application.ActivityLifecycleCallbacks life :
                    lifecycleCallbacks) {
                application.unregisterActivityLifecycleCallbacks(life);
            }
        }
    }

}
