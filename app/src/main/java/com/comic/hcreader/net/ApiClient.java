package com.comic.hcreader.net;

import com.comic.hcreader.model.Detail;
import com.comic.hcreader.model.Post;

import java.util.Map;

import io.realm.RealmList;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by zjy on 3/4/16.
 */
public interface ApiClient {
    @GET("chhreader/articles")
    Observable<RealmList<Post>> getPosts(@QueryMap Map<String, String> options);

    @GET("chhreader/content/{id}")
    Observable<Detail> getContent(@Path("id") int id);
}
