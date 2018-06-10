package com.gdut.dkmfromcg.commonlib.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.gdut.dkmfromcg.commonlib.activities.ProxyActivity;
import com.trello.rxlifecycle2.components.support.RxAppCompatDialogFragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragmentDelegate;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by dkmFromCG on 2018/4/10.
 * function:
 */

public abstract class BaseDialogFragment extends RxAppCompatDialogFragment implements ISupportFragment {

    //代理Fragment
    private final SupportFragmentDelegate DELEGATE = new SupportFragmentDelegate(this);
    protected FragmentActivity _mActivity = null;

    protected View rootView;
    protected Context mContext;
    @SuppressWarnings("SpellCheckingInspection")
    private Unbinder mUnbinder=null;

    public abstract Object setLayout();
    public abstract void onBindView(@Nullable Bundle savedInstanceState,View rootView);
    protected void initParams(Bundle bundle) {}
    protected void initDialogStyle(Dialog dialog, Window window) {}

    @Nullable
    @Override
    public Context getContext() {
        if (mContext != null) {
            return mContext;
        }
        return super.getContext();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext=context;
        DELEGATE.onAttach((Activity) context);
        _mActivity = DELEGATE.getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DELEGATE.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            initParams(getArguments());
        }
        if (setLayout() instanceof Integer){
            rootView=inflater.inflate((Integer) setLayout(),container,false);
        }else if (setLayout() instanceof View){
            rootView= (View) setLayout();
        }else {
            throw new ClassCastException("type of setLayout() must be int or View!");
        }
        if (rootView!=null){
            mUnbinder= ButterKnife.bind(this,rootView);
            onBindView(savedInstanceState,rootView);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DELEGATE.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DELEGATE.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                initDialogStyle(dialog, window);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DELEGATE.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        DELEGATE.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder!=null){
            mUnbinder.unbind();
        }
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            super.dismiss();
        }
    }

    @Override
    public void dismissAllowingStateLoss() {
        if (isShowing()) {
            super.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        DELEGATE.onHiddenChanged(hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        DELEGATE.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public SupportFragmentDelegate getSupportDelegate() {
        return DELEGATE;
    }

    @Override
    public ExtraTransaction extraTransaction() {
        return DELEGATE.extraTransaction();
    }

    @Override
    public void enqueueAction(Runnable runnable) {
        DELEGATE.enqueueAction(runnable);
    }

    @Override
    public void onEnterAnimationEnd(@Nullable Bundle savedInstanceState) {
        DELEGATE.onEnterAnimationEnd(savedInstanceState);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        DELEGATE.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onSupportVisible() {
        DELEGATE.onSupportVisible();
    }

    @Override
    public void onSupportInvisible() {
        DELEGATE.onSupportInvisible();
    }

    @Override
    public boolean isSupportVisible() {
        return DELEGATE.isSupportVisible();
    }


    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return DELEGATE.onCreateFragmentAnimator();
    }

    @Override
    public FragmentAnimator getFragmentAnimator() {
        return DELEGATE.getFragmentAnimator();
    }

    @Override
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        DELEGATE.setFragmentAnimator(fragmentAnimator);
    }

    @Override
    public void setFragmentResult(int resultCode, Bundle bundle) {
        DELEGATE.setFragmentResult(resultCode, bundle);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        DELEGATE.onFragmentResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewBundle(Bundle args) {
        DELEGATE.onNewBundle(args);
    }

    @Override
    public void putNewBundle(Bundle newBundle) {
        DELEGATE.putNewBundle(newBundle);
    }

    @Override
    public boolean onBackPressedSupport() {
        return DELEGATE.onBackPressedSupport();
    }

    public final ProxyActivity getProxyActivity() {
        return (ProxyActivity) _mActivity;
    }

    @Nullable
    public final <T extends View> T findViewById(@IdRes int id) {
        return rootView.findViewById(id);
    }

    public boolean isShowing() {
        return getDialog() != null && getDialog().isShowing();
    }

    public void dismiss(boolean isResume) {
        if (isResume) {
            dismiss();
        } else {
            dismissAllowingStateLoss();
        }
    }

}
