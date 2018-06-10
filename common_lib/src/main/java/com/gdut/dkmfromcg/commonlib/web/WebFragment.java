package com.gdut.dkmfromcg.commonlib.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.gdut.dkmfromcg.commonlib.fragments.ProxyFragment;
import com.gdut.dkmfromcg.commonlib.web.initImpl.IWebViewInitializer;
import com.gdut.dkmfromcg.commonlib.web.route.RouteKeys;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Created by dkmFromCG on 2018/3/25.
 * function:
 */

public abstract class WebFragment extends ProxyFragment implements IWebViewInitializer {

    private WebView mWebView = null;
    public String mUrl = null;
    private boolean mIsWebViewAvailable = false;
    private final ReferenceQueue<WebView> WEB_VIEW_QUEUE = new ReferenceQueue<>();
    private ProxyFragment mTopFragment = null;

    public void setTopFragment(ProxyFragment fragment) {
        mTopFragment = fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        mUrl = args.getString(RouteKeys.URL.name());
        initWebView();
    }

    /**
     * 初始化MWebView,在Xml中写的 WebView容易造成内存溢出
     */
    private void initWebView() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        } else {
            final WeakReference<WebView> webViewWeakReference =
                    new WeakReference<>(new WebView(getContext()), WEB_VIEW_QUEUE);
            mWebView = webViewWeakReference.get();
            mWebView = initWebView(mWebView);
            mWebView.setWebViewClient(initWebViewClient());
            mWebView.setWebChromeClient(initWebChromeClient());
            mIsWebViewAvailable = true;

        }
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {
        if (mWebView != null) {
            mWebView.onResume();
        }
        super.onResume();
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    /**
     * Gets the WebView.
     */
    public WebView getWebView() {
        if (mWebView == null) {
            throw new NullPointerException("WebView IS NULL!");
        }
        return mIsWebViewAvailable ? mWebView : null;
    }

    public ProxyFragment getTopFragment() {
        if (mTopFragment == null) {
            mTopFragment = this;
        }
        return mTopFragment;
    }


    public String getUrl() {
        if (mUrl == null) {
            throw new NullPointerException("Url IS NULL!");
        }
        return mUrl;
    }

}
