package com.gdut.dkmfromcg.commonlib.net;


import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by dkmFromCG on 2018/3/27.
 * function:
 */

public class RestServiceBuilder {

    /**
     * 单例模式
     */
    public static OkHttpClient mOkHttpClient;
    private static volatile RestServiceBuilder instance;
    private RestServiceBuilder(OkHttpClient okHttpClient){ mOkHttpClient=okHttpClient; }
    public static synchronized RestServiceBuilder getInstance(OkHttpClient okHttpClient) {

        if (instance == null) {
            synchronized (RestServiceBuilder.class) {
                if (instance == null) {
                    instance = new RestServiceBuilder(okHttpClient);
                }
            }
        }
        return instance;
    }


    public final RestService getRestService(){
        return RestServiceHolder.REST_SERVICE;
    }
    private static final class RestServiceHolder{
        private static final RestService REST_SERVICE
                = RetrofitHolder.RETROFIT_CLIENT.create(RestService.class);
    }


    private static final class RetrofitHolder{
        private static final String BASE_URL= ("http://play_holder_url.com/");
        private static final Retrofit RETROFIT_CLIENT=new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(mOkHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create()) //支持 转换为 String类型
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//支持RxJava
                .build();
    }
}
