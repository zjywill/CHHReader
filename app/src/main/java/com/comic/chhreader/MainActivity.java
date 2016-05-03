package com.comic.chhreader;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.comic.chhreader.component.BasicActivity;
import com.comic.chhreader.dataservice.DataService;
import com.comic.chhreader.model.Post;
import com.tbruyelle.rxpermissions.RxPermissions;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BasicActivity {

    private RealmRecyclerView mRealmRecyclerView;
    private RecyclerView mRecyclerView;
    private MainRealmRecyclerViewAdapter mMainRealmRecyclerViewAdapter;
    private Realm mRealm;
    private LinearLayoutManager mLinearLayoutManager;

    private int mCurrentPage = 1;
    private boolean isLoading = false;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxPermissions.getInstance(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                });
        initViews();
        refreshData();
    }

    private void initViews() {
        mRealmRecyclerView = (RealmRecyclerView) findViewById(R.id.realm_recycler_view);
        if (mRealmRecyclerView != null) {
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) mRealmRecyclerView.findViewById(R.id.rrv_swipe_refresh_layout);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
            }
            mRecyclerView = (RecyclerView) mRealmRecyclerView.findViewById(R.id.rrv_recycler_view);
            mRealmRecyclerView.setOnRefreshListener(this::refreshData);

            mRealm = Realm.getDefaultInstance();
            RealmResults<Post> datums = mRealm
                    .where(Post.class)
                    .findAllSorted("pk", Sort.DESCENDING);
            Loge.d("initViews post size: " + datums.size());
            mMainRealmRecyclerViewAdapter = new MainRealmRecyclerViewAdapter(this, datums);
            mRealmRecyclerView.setAdapter(mMainRealmRecyclerViewAdapter);
            setLoadMore();
        }
    }

    private void refreshData() {
        if (mRealmRecyclerView != null) {
            mRealmRecyclerView.setRefreshing(true);
        }
        if (mMainRealmRecyclerViewAdapter != null) {
            mMainRealmRecyclerViewAdapter.setRefreshing(true);
        }
        DataService.getInstance(this)
                .getPosts(true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MySubscriber.create(posts -> stopLoading()));
    }

    private void loadMore(String page) {
        Loge.d("loadMore page: " + page);
        DataService.getInstance(this)
                .getPosts(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MySubscriber.create(posts -> loadMoreSuccess(page)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRealm != null) {
            mRealm.close();
            mRealm = null;
        }
    }

    private void stopLoading() {
        mCurrentPage = 1;
        if (mMainRealmRecyclerViewAdapter != null) {
            mMainRealmRecyclerViewAdapter.setRefreshing(false);
        }
        if (mRealmRecyclerView != null) {
            mRealmRecyclerView.setRefreshing(false);
        }
    }

    private void setLoadMore() {
        if (mRecyclerView != null) {
            if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                mLinearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        totalItemCount = mLinearLayoutManager.getItemCount();
                        lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
                        if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            loadMore(String.format("%d", mCurrentPage + 1));
                            isLoading = true;
                        }
                    }
                });
            }
        }
    }

    private void loadMoreSuccess(String page) {
        Loge.d("loadMoreSuccess page: " + page);
        mCurrentPage = Integer.parseInt(page);
        isLoading = false;
    }
}
