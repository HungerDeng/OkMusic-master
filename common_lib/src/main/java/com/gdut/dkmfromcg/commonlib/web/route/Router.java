package com.gdut.dkmfromcg.commonlib.web.route;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.webkit.URLUtil;
import android.webkit.WebView;

import com.gdut.dkmfromcg.commonlib.fragments.ProxyFragment;
import com.gdut.dkmfromcg.commonlib.web.WebFragment;
import com.gdut.dkmfromcg.commonlib.web.WebFragmentImpl;

/**
 * Created by dkmFromCG on 2018/3/25.
 * function:
 */

public class Router {

    private Router() {
    }

    private static class Holder {
        private static final Router INSTANCE = new Router();
    }

    public static Router getInstance() {
        return Holder.INSTANCE;
    }

    public final void loadPage(WebFragment fragment, String url) {
        loadPage(fragment.getWebView(), url);
    }

    public final boolean handleWebUrl(WebFragment fragment, String url) {

        //如果是电话协议
        if (url.contains("tel:")) {
            callPhone(fragment.getContext(), url);
            return true;
        }

        final ProxyFragment topFragment = fragment.getTopFragment();
        final WebFragmentImpl webFragment = WebFragmentImpl.create(url);
        topFragment.getSupportDelegate().start(webFragment);

        return true;
    }

    private void loadWebPage(WebView webView, String url) {
        if (webView != null) {
            webView.loadUrl(url);
        } else {
            throw new NullPointerException("WebView is null!");
        }
    }

    private void loadLocalPage(WebView webView, String url) {
        loadWebPage(webView, "file:///android_asset/" + url);
    }

    private void loadPage(WebView webView, String url) {
        if (URLUtil.isNetworkUrl(url) || URLUtil.isAssetUrl(url)) {
            loadWebPage(webView, url);
        } else {
            loadLocalPage(webView, url);
        }
    }

    private void callPhone(Context context, String uri) {
        final Intent intent = new Intent(Intent.ACTION_DIAL);
        final Uri data = Uri.parse(uri);
        intent.setData(data);
        ContextCompat.startActivity(context, intent, null);
    }

}
