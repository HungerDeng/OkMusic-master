package com.gdut.dkmfromcg.commonlib.net;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;


/**
 * Created by dkmFromCG on 2018/3/12.
 * function:
 */

public interface RestService {


    @GET
    Observable<String> get(@Url String url, @QueryMap Map<String, Object> params); //@QueryMap在@GET 时起到字符串拼接作用

    @FormUrlEncoded
    @POST
    Observable<String> post(@Url String url, @FieldMap Map<String, Object> params);//@FieldMap在@POST 时起到字符串拼接作用

    @POST
    Observable<String> postRaw(@Url String url, @Body RequestBody body); //传入原始数据时,不可以添加 @FormUrlEncoded 注解

    @FormUrlEncoded
    @PUT
    Observable<String> put(@Url String url, @FieldMap Map<String, Object> params);

    @PUT
    Observable<String> putRaw(@Url String url, @Body RequestBody body);

    @DELETE
    Observable<String> delete(@Url String url, @QueryMap Map<String, Object> params);

    @Streaming //下载时,默认是先下载到内存区,下载完成后再写入文件,这样就会造成内存溢出.而 @Streaming避免这种情况,可以一边下载一边存入文件
    @GET
    Observable<ResponseBody> download(@Url String url, @QueryMap Map<String, Object> params);

    @Multipart
    @POST
    Observable<String> upload(@Url String url, @Part MultipartBody.Part file);

}
