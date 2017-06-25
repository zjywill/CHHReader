package com.comic.hcreader.net;

import com.comic.hcreader.BuildConfig;
import com.comic.hcreader.component.BasicActivityContainer;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zjy on 3/4/16.
 */
public class ApiService extends BasicActivityContainer {
    private Retrofit mRetrofitWithGsonAndRxJava;
    private Gson mGsonUsingRealm;
    private String mHost;

    public ApiService() {
        mHost = ApiConfig.API_URL;
    }

    public ApiService(String host) {
        mHost = host;
    }

    public Retrofit getRetrofitWithGsonAndRxJava() {
        if (mRetrofitWithGsonAndRxJava == null) {
            rebuildClient();
        }
        return mRetrofitWithGsonAndRxJava;
    }

    public ApiClient getApiClient() {
        Retrofit retrofit = getRetrofitWithGsonAndRxJava();
        ApiClient apiClient = retrofit.create(ApiClient.class);
        return apiClient;
    }

    public void setHost(String newHost) {
        if (mHost == null || !newHost.contentEquals(mHost)) {
            mHost = newHost;
            rebuildClient();
        }
    }

    private void rebuildClient() {
         HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        mRetrofitWithGsonAndRxJava = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(mHost != null ? mHost : ApiConfig.API_URL)
                .client(httpClient)
                .build();
    }
}
