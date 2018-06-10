package com.gdut.dkmfromcg.commonlib.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gdut.dkmfromcg.commonlib.web.initImpl.WebChromeClientInitImpl;
import com.gdut.dkmfromcg.commonlib.web.initImpl.WebViewClientInitImpl;
import com.gdut.dkmfromcg.commonlib.web.initImpl.WebViewInitImpl;
import com.gdut.dkmfromcg.commonlib.web.route.RouteKeys;
import com.gdut.dkmfromcg.commonlib.web.route.Router;


/**
 * Created by dkmFromCG on 2018/3/25.
 * function: WebFragment的具体实现
 */

public class WebFragmentImpl extends WebFragment  {


    private IPageLoadListener mIPageLoadListener = null;


    public static WebFragmentImpl create(String url){
        final Bundle args = new Bundle();
        args.putString(RouteKeys.URL.name(), url);
        final WebFragmentImpl webFragment=new WebFragmentImpl();
        webFragment.setArguments(args);
        return webFragment;
    }

    public void setPageLoadListener(IPageLoadListener listener) {
        this.mIPageLoadListener = listener;
    }

    @Override
    public Object setLayout() {
        return getWebView();
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        //加载 Url
        if (getUrl()!=null){
            Router.getInstance().loadPage(this,getUrl());
        }
    }

    @Override
    public WebView initWebView(WebView webView) {
        return new WebViewInitImpl().createWebView(webView);
    }

    @Override
    public WebViewClient initWebViewClient() {
        final WebViewClientInitImpl client = new WebViewClientInitImpl(this);
        client.setPageLoadListener(mIPageLoadListener);
        return client;
    }

    @Override
    public WebChromeClient initWebChromeClient() {
        return new WebChromeClientInitImpl();
    }
}
