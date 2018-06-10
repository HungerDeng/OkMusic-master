package com.gdut.dkmfromcg.okmusic.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.gdut.dkmfromcg.commonlib.activities.ProxyActivity;
import com.gdut.dkmfromcg.commonlib.fragments.ProxyFragment;
import com.gdut.dkmfromcg.okmusic.R;
import com.gdut.dkmfromcg.okmusic.R2;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import butterknife.BindView;

public class MainActivity extends ProxyActivity {


    @BindView(R2.id.toolbar)
    Toolbar toolbar = null;
    @BindView(R2.id.appbar_layout)
    AppBarLayout appbarLayout = null;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout = null;
    @BindView(R2.id.search_view)
    MaterialSearchView searchView = null;
    @BindView(R2.id.ad_container)
    FrameLayout adContainer;


    @Override
    public ProxyFragment setRootFragment() {
        return null;
    }

    @Override
    public Integer setLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState) {
        initAppBar();
        initSearchView();


/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RxPermissionsTool.
                    with(this).
                    addPermission(Manifest.permission.READ_EXTERNAL_STORAGE).
                    addPermission(Manifest.permission.CAMERA).
                    addPermission(Manifest.permission.CALL_PHONE).
                    initPermission();
        }*/


    }


    private void initAppBar() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
    }

    private void initSearchView() {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_main_search);
        searchView.setMenuItem(item);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            return;
        }
        super.onBackPressed();

    }

}
