package com.comic.hcreader.component;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import rx.Subscription;

/**
 * Created by junyi on 3/22/16.
 */
public class BasicFragment extends Fragment {

    protected Subscription mMainActivitySubscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMainActivitySubscription != null && !mMainActivitySubscription.isUnsubscribed())
            mMainActivitySubscription.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
