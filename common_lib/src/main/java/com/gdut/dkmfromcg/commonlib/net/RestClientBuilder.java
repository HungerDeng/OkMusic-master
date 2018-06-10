package com.gdut.dkmfromcg.commonlib.net;

import android.content.Context;
import android.support.annotation.NonNull;


import com.gdut.dkmfromcg.commonlib.net.interceptors.CacheInterceptor;
import com.gdut.dkmfromcg.commonlib.net.interceptors.CacheInterceptorOffline;
import com.gdut.dkmfromcg.commonlib.util.log.Logger;
import com.trello.rxlifecycle2.LifecycleProvider;


import java.io.File;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by dkmFromCG on 2018/3/12.
 * function:
 */

public final class RestClientBuilder {
    private static final int DEFAULT_TIMEOUT = 3;
    private static final String KEY_CACHE = "Http_Cache";
    private static final long DEFAULT_CACHE_MAXSIZE = 10 * 1024 * 1024;
    private static final int DEFAULT_MAX_STALE = 60 * 60 * 24 * 3;
    private static final int DEFAULT_MAXIDLE_CONNECTIONS = 5;
    private static final long DEFAULT_KEEP_ALIVEDURATION = 8;

    /**
     * OKHttpBuilder 设置属性
     */
    private int mConnectTimeout = DEFAULT_TIMEOUT;
    private int mWriteTimeout = DEFAULT_TIMEOUT;
    private int mReadTimeout = DEFAULT_TIMEOUT;
    private Boolean mIsLog = true;
    private Boolean mIsCookie = false;
    private Boolean mIsCache = false;
    private Boolean mIsSkip = true;
    //设置缓存
    private Cache mCache = null;
    private long mCacheMaxSize = DEFAULT_CACHE_MAXSIZE;
    private File mHttpCacheDirectory =null;
    private int mCacheTimeout = DEFAULT_MAX_STALE;
    //设置回收池
    private ConnectionPool mConnectionPool;
    private int default_maxidle_connections = DEFAULT_MAXIDLE_CONNECTIONS;
    private long default_keep_aliveduration = DEFAULT_KEEP_ALIVEDURATION;
    //设置拦截器
    private final List<Interceptor> INTERCEPTOR_LIST;
    private Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR;
    private Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE;


    private Context mContext = null;
    private String mUrl = null;
    private final WeakHashMap<String, Object> PARAMS;
    private RequestBody mBody = null;
    private File mFile = null; //upload文件时,需要的参数
    /*download时,需要用到的参数*/
    private String mDownloadDir = null;
    private String mExtension = null;
    private String mName = null;

    //用于防止内存溢出
    private LifecycleProvider mLifecycleProvider=null;

    private final OkHttpClient.Builder OK_HTTP_ClIENT_BUILDER;
    //不定义为public,使之不能在外部直接new出来
    RestClientBuilder() {
        INTERCEPTOR_LIST= RestClientBuilderHelper.getInstance().getInterceptors();
        PARAMS = RestClientBuilderHelper.getInstance().getParams();
        OK_HTTP_ClIENT_BUILDER = RestClientBuilderHelper.getInstance().getOkHttpBuilder();
    }

    public final RestClientBuilder connectTimeout(int connectTimeout) {
        this.mConnectTimeout = connectTimeout;
        return this;
    }

    public final RestClientBuilder writeTimeout(int writeTimeout) {
        this.mWriteTimeout = writeTimeout;
        return this;
    }

    public final RestClientBuilder readTimeout(int readTimeout) {
        this.mReadTimeout = readTimeout;
        return this;
    }

    public final RestClientBuilder isLog(boolean isLog) {
        this.mIsLog = isLog;
        return this;
    }

    public final RestClientBuilder isCookie(boolean isCookie) {
        this.mIsCookie = isCookie;
        return this;
    }

    public final RestClientBuilder isCache(boolean isCache) {
        this.mIsCache = isCache;
        return this;
    }

    public final RestClientBuilder isSkip(boolean isSkip) {
        this.mIsSkip = isSkip;
        return this;
    }

    public final RestClientBuilder addCache(Cache cache) {
        return addCache(cache, mCacheTimeout);
    }

    public RestClientBuilder addCache(Cache cache, final int cacheTimeOut) {
        addCache(cache, String.format("max-age=%d", cacheTimeOut));
        return this;
    }

    private void addCache(Cache cache, final String cacheControlValue) {
        REWRITE_CACHE_CONTROL_INTERCEPTOR = new CacheInterceptor(mContext, cacheControlValue);
        REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE = new CacheInterceptorOffline(mContext, cacheControlValue);
        OK_HTTP_ClIENT_BUILDER.addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR);
        OK_HTTP_ClIENT_BUILDER.addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE);
        interceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE);
        this.mCache = cache;
    }

    public final RestClientBuilder addCacheMaxSize(int size) {
        this.mCacheMaxSize = size;
        return this;
    }

    /**
     * Sets the connection pool used to recycle HTTP and HTTPS connections.
     */
    public final RestClientBuilder connectionPool(@NonNull ConnectionPool connectionPool) {
        this.mConnectionPool = connectionPool;
        return this;
    }

    public final RestClientBuilder interceptor(Interceptor interceptor){
        this.INTERCEPTOR_LIST.add(interceptor);
        return this;
    }

    public final RestClientBuilder interceptors(List<Interceptor> interceptors){
        this.INTERCEPTOR_LIST.addAll(interceptors);
        return this;
    }

    public final RestClientBuilder context(Context context) {
        this.mContext = context;
        return this;
    }

    public final RestClientBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final RestClientBuilder params(WeakHashMap<String, Object> params) {
        PARAMS.putAll(params);
        return this;
    }

    public final RestClientBuilder params(String key, Object value) {
        PARAMS.put(key, value);
        return this;
    }

    public final RestClientBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final RestClientBuilder file(String file) {
        this.mFile = new File(file);
        return this;
    }

    public final RestClientBuilder name(String name) {
        this.mName = name;
        return this;
    }

    public final RestClientBuilder dir(String dir) {
        this.mDownloadDir = dir;
        return this;
    }

    public final RestClientBuilder extension(String extension) {
        this.mExtension = extension;
        return this;
    }

    //传入获取的原始json数据
    public final RestClientBuilder raw(String raw) {
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), raw);
        return this;
    }

    public final RestClientBuilder setLifecycleProvider(LifecycleProvider lifecycleProvider){
        this.mLifecycleProvider=lifecycleProvider;
        return this;
    }


    public final RestClient build() {
        setLog();
        setSkipSSLVerifier();
        setCache();
        setCookie();
        OK_HTTP_ClIENT_BUILDER.connectTimeout(mConnectTimeout, TimeUnit.SECONDS);
        OK_HTTP_ClIENT_BUILDER.writeTimeout(mWriteTimeout,TimeUnit.SECONDS);
        OK_HTTP_ClIENT_BUILDER.readTimeout(mReadTimeout,TimeUnit.SECONDS);
        for (Interceptor interceptor:INTERCEPTOR_LIST){
            OK_HTTP_ClIENT_BUILDER.addInterceptor(interceptor);
        }
        setRecyclerPool();
        final OkHttpClient okHttpClient=OK_HTTP_ClIENT_BUILDER.build();
        if (mLifecycleProvider==null){
            Logger.d("RestClientBuilder.build(): ","LifecycleProvider 为null,会造成内存溢出.");
        }

        return new RestClient(mContext, mUrl, PARAMS,
                mBody, mFile,  mDownloadDir, mExtension, mName,okHttpClient,mLifecycleProvider);
    }


    /**
     * 是否可打印 Log
     */
    private void setLog() {
        if (mIsLog){
            OK_HTTP_ClIENT_BUILDER.addNetworkInterceptor(
                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS));
            OK_HTTP_ClIENT_BUILDER.addNetworkInterceptor(
                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
    }

    /**
     * 是否跳过 SSL 认证
     */
    private void setSkipSSLVerifier() {
        if (mIsSkip){
            OK_HTTP_ClIENT_BUILDER.sslSocketFactory(HttpsFactroy.getSSLSocketFactory(),
                    HttpsFactroy.creatX509TrustManager());
            OK_HTTP_ClIENT_BUILDER.hostnameVerifier(HttpsFactroy.creatSkipHostnameVerifier());
        }
    }

    private void setCache() {
        if (mIsCache){
            //存储目录
            if (mHttpCacheDirectory == null) {
                mHttpCacheDirectory = new File(mContext.getCacheDir(), KEY_CACHE);
            }
            //存储区
            try{
                if (mCache == null) {
                    mCache = new Cache(mHttpCacheDirectory, mCacheMaxSize);
                }
                addCache(mCache);
            }catch (Exception e){
                Logger.e("OKHttpClientBuilder", "Could not create http cache");
                e.printStackTrace();
            }
            if (mCache == null) {
                mCache = new Cache(mHttpCacheDirectory, mCacheMaxSize);
            }
        }
        //防止 添加了Cache ,但是忘记把 mIsCache 设置为 true
        if (mCache!=null){
            OK_HTTP_ClIENT_BUILDER.cache(mCache);
        }
    }

    private void setCookie() {
        if (mIsCookie){

        }
    }

    private void setRecyclerPool() {
        if (mConnectionPool==null){
            mConnectionPool=new ConnectionPool(default_maxidle_connections, default_keep_aliveduration, TimeUnit.SECONDS);
        }
        OK_HTTP_ClIENT_BUILDER.connectionPool(mConnectionPool);
    }

}
