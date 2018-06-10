package com.gdut.dkmfromcg.commonlib.fragments;

/**
 * Created by dkmFromCG on 2018/3/11.
 * function:
 */

public abstract class ProxyFragment extends PermissionCheckerFragment {



    @SuppressWarnings("unchecked")
    public <T extends ProxyFragment> T getParentFrag() {
        return (T) getParentFragment();
    }

}
