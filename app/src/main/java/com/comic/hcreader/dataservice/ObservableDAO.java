package com.comic.hcreader.dataservice;

import android.content.Context;

import com.comic.hcreader.Loge;
import com.comic.hcreader.model.Post;
import com.comic.hcreader.rxrealm.RealmObservable;

import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by zjy on 3/4/16.
 */
public class ObservableDAO {

    private Context mContext;

    public ObservableDAO(Context context) {
        this.mContext = context;
    }

    public Observable<RealmList<Post>> readPosts() {
        return RealmObservable.list(mContext, realm -> {

            RealmResults<Post> postsRealmResults = realm.where(Post.class).findAll();

            //cast RealmResults into RealmList
            RealmList<Post> postsRealmList = new RealmList<>();
            if (postsRealmResults != null) {
                Loge.d("readPosts get all postsRealmResults size: " + postsRealmResults.size());
                for (Post post : postsRealmResults) {
                    postsRealmList.add(post);
                }
            }
            return postsRealmList;
        });
    }

    public Observable<RealmList<Post>> writePosts(final RealmList<Post> posts, final boolean forceUpdate) {
        return RealmObservable.list(mContext, realm -> {
            if (posts != null && posts.size() > 0) {
                Loge.d("writePosts posts: " + posts.size());
                if (forceUpdate) {
                    RealmResults<Post> realmResults = realm.where(Post.class).findAll();
                    Loge.d("writePosts force update remove former data: " + realmResults.size());
                    realmResults.deleteAllFromRealm();
                }
                realm.copyToRealmOrUpdate(posts);
            }
            return posts;
        });
    }


}
