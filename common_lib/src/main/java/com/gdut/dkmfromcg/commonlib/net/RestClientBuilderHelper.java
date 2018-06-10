package com.gdut.dkmfromcg.commonlib.net;



import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;


/**
 * Created by dkmFromCG on 2018/3/12.
 * function:
 */

public final class RestClientBuilderHelper {

    /**
     * 单例模式
     */
    private RestClientBuilderHelper(){}
    private static final class Holder{
        private static final RestClientBuilderHelper INSTANCE=new RestClientBuilderHelper();
    }
    public static RestClientBuilderHelper getInstance(){
        return Holder.INSTANCE;
    }


    //url 的参数
    public final WeakHashMap<String,Object> getParams(){
        return ParamsHolder.PARAMS;
    }
    private static final class ParamsHolder{
        private static final WeakHashMap<String,Object> PARAMS=new WeakHashMap<>();
    }

    public final List<Interceptor> getInterceptors(){
        return InterceptorHolder.INTERCEPTORS;
    }
    private static final class InterceptorHolder{
        private static final List<Interceptor> INTERCEPTORS = new ArrayList<>();
    }


    public final OkHttpClient.Builder getOkHttpBuilder(){
        return OkHttpHolder.BUILDER;
    }
    private static final class OkHttpHolder{
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();
    }

}
