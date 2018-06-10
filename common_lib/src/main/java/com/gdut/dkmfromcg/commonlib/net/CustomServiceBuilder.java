package com.gdut.dkmfromcg.commonlib.net;


import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * Created by dkmFromCG on 2018/3/27.
 * function:
 */

public class CustomServiceBuilder {

    private static String mBaseUrl=null;
    /**
     * 单例模式
     */
    public static OkHttpClient mOkHttpClient;
    private static volatile CustomServiceBuilder instance;
    private CustomServiceBuilder(OkHttpClient okHttpClient){ mOkHttpClient=okHttpClient; }
    public static synchronized CustomServiceBuilder getInstance(OkHttpClient okHttpClient) {

        if (instance == null) {
            synchronized (CustomServiceBuilder.class) {
                if (instance == null) {
                    instance = new CustomServiceBuilder(okHttpClient);
                }
            }
        }
        return instance;
    }


    public final <T> T getRestService(Class<T> service,String baseUrl){
        mBaseUrl=baseUrl;
        return RetrofitHolder.RETROFIT_CLIENT.create(service);
    }

    private static final class RetrofitHolder{
        private static final Retrofit RETROFIT_CLIENT=new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .client(mOkHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())//支持 转换为 String类型
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//支持RxJava
                .build();
    }
}
