package com.gdut.dkmfromcg.commonlib.fragments;

import android.Manifest;
import android.support.annotation.NonNull;

import com.gdut.dkmfromcg.commonlib.util.callback.CallbackManager;
import com.gdut.dkmfromcg.commonlib.util.callback.CallbackType;
import com.gdut.dkmfromcg.commonlib.util.callback.IGlobalCallback;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by dkmFromCG on 2018/3/11.
 * function:
 */

@RuntimePermissions
public abstract class PermissionCheckerFragment extends BaseFragment {

    //不是直接调用方法
    @NeedsPermission({Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void startCamera() {
        final IGlobalCallback callback=CallbackManager.getInstance().getCallback(CallbackType.ON_CROP);
        if (callback!=null){
            callback.executeCallback(null);
        }
    }

    //这个是真正调用的方法
    public void startCameraWithCheck() {
        PermissionCheckerFragmentPermissionsDispatcher.startCameraWithPermissionCheck(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionCheckerFragmentPermissionsDispatcher.onRequestPermissionsResult(this,requestCode, grantResults);
    }
}
