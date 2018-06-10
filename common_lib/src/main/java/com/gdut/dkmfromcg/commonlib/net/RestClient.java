package com.gdut.dkmfromcg.commonlib.net;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.gdut.dkmfromcg.commonlib.net.callback.RequestCallback;
import com.gdut.dkmfromcg.commonlib.net.download.SaveFileTask;
import com.gdut.dkmfromcg.commonlib.net.exception.ApiException;
import com.gdut.dkmfromcg.commonlib.net.exception.HttpResultFunction;
import com.gdut.dkmfromcg.commonlib.net.callback.RxObserver;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.io.File;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


/**
 * Created by dkmFromCG on 2018/3/12.
 * function:
 */

public final class RestClient {

    private final Context CONTEXT;
    private final String URL;
    private static final WeakHashMap<String, Object> PARAMS = RestClientBuilderHelper.getInstance().getParams();
    private final RequestBody BODY; //传递原始数据时,BODY 不为 null
    private final File FILE;  //upload文件时,需要的参数
    /*download时,需要用到的参数*/
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String NAME;

    private final OkHttpClient OKHTTP_CLIENT;
    private final LifecycleProvider LIFECYCLE_PROVIDER;

    RestClient(Context context,
               String url,
               WeakHashMap<String, Object> params,
               RequestBody body,
               File file,
               String downloadDir,
               String extension,
               String name,
               OkHttpClient okHttpClient,
               @NonNull LifecycleProvider lifecycleProvider
    ) {
        this.CONTEXT = context;
        this.URL = url;
        PARAMS.putAll(params);
        this.BODY = body;
        this.FILE = file;
        this.DOWNLOAD_DIR = downloadDir;
        this.EXTENSION = extension;
        this.NAME = name;
        this.OKHTTP_CLIENT = okHttpClient;
        this.LIFECYCLE_PROVIDER = lifecycleProvider;
    }


    public static RestClientBuilder builder() {
        return new RestClientBuilder();
    }

    public final void get(RxObserver<String> observer) {
        request(HttpMethod.GET, observer);
    }


    public final void post(RxObserver<String> observer) {

        if (BODY == null) {
            request(HttpMethod.POST, observer);
        } else {
            if (!PARAMS.isEmpty()) { // post 原始数据时,params一定要为空
                throw new RuntimeException("params must be null!");
            }
            request(HttpMethod.POST_RAW, observer);
        }

    }

    public final void put(RxObserver<String> observer) {

        if (BODY == null) {
            request(HttpMethod.PUT, observer);
        } else {
            if (!PARAMS.isEmpty()) { // put 原始数据时,params一定要为空
                throw new RuntimeException("params must be null!");
            }
            request(HttpMethod.PUT_RAW, observer);
        }
    }

    public final void delete(RxObserver<String> observer) {
        request(HttpMethod.DELETE, observer);
    }

    public final void upload(RxObserver<String> observer) {
        request(HttpMethod.UPLOAD, observer);
    }

    @SuppressWarnings("unchecked")
    public final void download(final RequestCallback<String> requestCallback) {
        RestServiceBuilder.getInstance(OKHTTP_CLIENT).getRestService()
                .download(URL, PARAMS)
                .compose(LIFECYCLE_PROVIDER.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxObserver<ResponseBody>() {
                    @Override
                    protected void onStart(Disposable d) {
                        if (requestCallback != null) {
                            requestCallback.onRequestStart();
                        }
                    }

                    @Override
                    protected void onSuccess(ResponseBody response) {
                        final SaveFileTask task = new SaveFileTask(CONTEXT, requestCallback);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                DOWNLOAD_DIR, EXTENSION, response, NAME);
                        if (task.isCancelled()) {
                            cancel();
                            if (requestCallback != null) {
                                requestCallback.onRequestEnd();
                            }
                        }
                        if (requestCallback != null) {
                            requestCallback.onRequestEnd();
                        }
                    }

                    @Override
                    protected void onError(ApiException e) {
                        if (requestCallback != null) {
                            requestCallback.onError(e);
                        }
                        if (requestCallback != null) {
                            requestCallback.onRequestEnd();
                        }
                    }
                });
    }


    @SuppressWarnings("unchecked")
    private void request(HttpMethod method, RxObserver<String> observer) {
        final RestService service = RestServiceBuilder.getInstance(OKHTTP_CLIENT).getRestService();
        Observable<String> observable = null;

        switch (method) {
            case GET:
                observable = service.get(URL, PARAMS);
                break;
            case POST:
                observable = service.post(URL, PARAMS);
                break;
            case POST_RAW:
                observable = service.postRaw(URL, BODY);
                break;
            case PUT:
                observable = service.put(URL, PARAMS);
                break;
            case PUT_RAW:
                observable = service.putRaw(URL, BODY);
                break;
            case DELETE:
                observable = service.delete(URL, PARAMS);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), FILE);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", FILE.getName(), requestBody);
                observable = service.upload(URL, body);
                break;
            default:
                break;
        }
        if (observable == null) return;
        observable.compose(LIFECYCLE_PROVIDER.bindToLifecycle())
                .onErrorResumeNext(new HttpResultFunction<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    //自定义 APIService
    public final <T> T createCustomService(final Class<T> customService, @NonNull final String baseUrl) {
        final T service = CustomServiceBuilder.getInstance(OKHTTP_CLIENT).getRestService(customService, baseUrl);
        return service;
    }

    @SuppressWarnings("unchecked")
    public final <T> void call(Observable<T> observable, RxObserver<T> observer) {
        observable.compose(LIFECYCLE_PROVIDER.bindToLifecycle())
                .onErrorResumeNext(new HttpResultFunction<T>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
