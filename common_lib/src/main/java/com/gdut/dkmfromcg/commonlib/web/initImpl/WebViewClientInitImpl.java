package com.gdut.dkmfromcg.commonlib.web.initImpl;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.gdut.dkmfromcg.commonlib.web.IPageLoadListener;
import com.gdut.dkmfromcg.commonlib.web.WebFragment;
import com.gdut.dkmfromcg.commonlib.util.log.Logger;

/**
 * Created by dkmFromCG on 2018/3/25.
 * function:
 */

public class WebViewClientInitImpl extends WebViewClient {

    private final WebFragment FRAGMENT;
    private IPageLoadListener mIPageLoadListener = null;

    public WebViewClientInitImpl(WebFragment fragment) {
        this.FRAGMENT = fragment;
    }

    public void setPageLoadListener(IPageLoadListener listener) {
        this.mIPageLoadListener = listener;
    }

    /**
     * @param view
     * @param url
     * @return True if the host application wants to leave the current WebView
     * and handle the url itself, otherwise return false.
     * 返回true时在本APP内打开 webView,否则打开 Activity Manager 选择适当的程序打开webView
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Logger.d("shouldOverrideUrlLoading", url);
        view.loadUrl(url);
        return true;
        //return Router.getInstance().handleWebUrl(FRAGMENT, url);
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (mIPageLoadListener != null) {
            mIPageLoadListener.onLoadStart();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (mIPageLoadListener != null) {
            mIPageLoadListener.onLoadEnd();
        }
    }
}
