package com.gdut.dkmfromcg.commonlib.util.router;

import com.alibaba.android.arouter.launcher.ARouter;

/**
 * Created by dkmFromCG on 2018/4/29.
 * function:
 */

public class RouterUtil {

    public static <T> T navigation(String path) {
        return (T) ARouter.getInstance().build(path).navigation();
    }
}
