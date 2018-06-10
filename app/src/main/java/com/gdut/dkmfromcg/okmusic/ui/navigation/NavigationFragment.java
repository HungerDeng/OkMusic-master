package com.gdut.dkmfromcg.okmusic.ui.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.gdut.dkmfromcg.commonlib.fragments.ProxyFragment;
import com.gdut.dkmfromcg.okmusic.R;

/**
 * Created by dkmFromCG on 2018/5/30.
 * function:
 */

public class NavigationFragment extends ProxyFragment implements INavigationView{
    @Override
    public Object setLayout() {
        return R.layout.fragment_navigation;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {

    }
}
