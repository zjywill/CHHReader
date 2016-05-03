package com.comic.chhreader;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.comic.chhreader.model.Post;

import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class MainRealmRecyclerViewAdapter extends RealmBasedRecyclerViewAdapter<Post, MainRealmRecyclerViewHolder> {

    private boolean isRefreshing = false;

    public MainRealmRecyclerViewAdapter(
            Context context,
            RealmResults<Post> realmResults) {
        super(context, realmResults, true, true, null);
    }


    @Override
    public MainRealmRecyclerViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.content_list_item, viewGroup, false);
        MainRealmRecyclerViewHolder vh = new MainRealmRecyclerViewHolder(v);
        return vh;
    }

    @Override
    public void onBindRealmViewHolder(MainRealmRecyclerViewHolder itemViewHolder, int position) {
        Post post = realmResults.get(position);
        Post prePost = null;
        if (position > 0) {
            prePost = realmResults.get(position - 1);
        }
        itemViewHolder.bindItem(post, prePost);
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        isRefreshing = refreshing;
    }
}