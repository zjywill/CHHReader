package com.comic.hcreader.dataservice;

import android.content.Context;
import android.text.TextUtils;

import com.comic.hcreader.Loge;
import com.comic.hcreader.model.Detail;
import com.comic.hcreader.model.Post;
import com.comic.hcreader.net.ApiConfig;
import com.comic.hcreader.net.ApiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import rx.Observable;


/**
 * Created by zjy on 3/4/16.
 */
public class DataService {
    private static DataService sInstance;

    private String mHost;
    private ObservableDAO mDAO;
    private ApiService mApiService;
    private Context mContext;

    private DataService(Context context) {
        mContext = context;
    }

    public static DataService getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataService(context);
        }
        return sInstance;
    }

    public ApiService getApiService() {
        if (mApiService == null) {
            mApiService = new ApiService();
        }
        return mApiService;
    }

    public ObservableDAO getObservableDAO() {
        if (mDAO == null) {
            mDAO = new ObservableDAO(mContext);
        }
        return mDAO;
    }


    public void setHost(String host) {
        mHost = host;
        mApiService.setHost(host);
    }

    public String getHost() {
        return (mHost != null && mHost.length() > 0) ? mHost : ApiConfig.API_URL;
    }

    public Observable<List<Post>> getPosts(final boolean forceUpdate) {
        Loge.d("getPosts in forceUpdate: " + forceUpdate);
        final Map<String, String> queryMap = new HashMap<>();
        final Observable<RealmList<Post>> apiObservable = getApiService().getApiClient().getPosts(queryMap);
        final Observable<RealmList<Post>> dbObservable = getObservableDAO().readPosts();

        return dbObservable.exists(postRealmResults -> postRealmResults.size() > 0)
                .flatMap(inDB -> (inDB && !forceUpdate) ? dbObservable : apiObservable)
                .flatMap(posts -> getObservableDAO().writePosts(posts, forceUpdate))
                .map(topics -> {
                    List<Post> newTopics = new ArrayList<>();
                    for (Post topic : topics) {
                        newTopics.add(topic);
                    }
                    return newTopics;
                });
    }

    public Observable<List<Post>> getPosts(final String page) {
        final Map<String, String> queryMap = new HashMap<>();
        if (!TextUtils.isEmpty(page)) {
            queryMap.put("page", page);
        }
        final Observable<RealmList<Post>> apiObservable = getApiService().getApiClient().getPosts(queryMap);
        return apiObservable
                .flatMap(posts -> getObservableDAO().writePosts(posts, false))
                .map(topics -> {
                    List<Post> newTopics = new ArrayList<>();
                    for (Post topic : topics) {
                        newTopics.add(topic);
                    }
                    return newTopics;
                });
    }

    public Observable<Detail> getContent(int id) {
        return getApiService().getApiClient().getContent(id);
    }
}
