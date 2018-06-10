package com.gdut.dkmfromcg.okmusic.presenter.navigation;


import com.gdut.dkmfromcg.commonlib.mvp.BasePresenter;
import com.gdut.dkmfromcg.okmusic.ui.navigation.INavigationView;

/**
 * Created by dkmFromCG on 2018/5/15.
 * function:
 */

public class NavigationPresenterImpl extends BasePresenter<INavigationView> implements INavigationPresenter {
    public NavigationPresenterImpl(INavigationView view) {
        super(view);
    }
}
