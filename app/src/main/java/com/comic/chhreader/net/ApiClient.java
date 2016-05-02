package com.comic.chhreader.net;

import com.comic.chhreader.model.Detail;
import com.comic.chhreader.model.Post;

import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by zjy on 3/4/16.
 */
public interface ApiClient {
    @GET("chhreader/getpostdata")
    Observable<RealmList<Post>> getPosts(@QueryMap Map<String, String> options);

    @GET("chhreader/getcontent")
    Observable<List<Detail>> getContent(@QueryMap Map<String, String> options);
}
