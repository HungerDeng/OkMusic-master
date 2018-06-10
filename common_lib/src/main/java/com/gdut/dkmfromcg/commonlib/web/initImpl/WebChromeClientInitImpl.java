package com.gdut.dkmfromcg.commonlib.web.initImpl;

import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by dkmFromCG on 2018/3/25.
 * function:
 */

public class WebChromeClientInitImpl extends WebChromeClient {

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }
}
