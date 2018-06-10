package com.gdut.dkmfromcg.commonlib.util.callback;

import android.support.annotation.Nullable;

/**
 * Created by dkmFromCG on 2018/4/3.
 * function:
 */

public interface IGlobalCallback<T> {

    void executeCallback(@Nullable T args);
}
