package com.comic.chhreader.component;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import rx.Subscription;

/**
 * Created by junyi on 3/22/16.
 */
public class BasicActivity extends AppCompatActivity {

    private boolean mCreating;
    protected Subscription mMainActivitySubscription;
    protected Drawable mDividerDrawable;
    protected boolean isPause;
    protected int mShadowColor;
    protected long mActivityEnterTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchContext();
        mCreating = true;
    }

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mCreating) {
            switchContext();
        }
        mActivityEnterTime = System.currentTimeMillis();
        isPause = false;
        mCreating = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    protected void switchContext() {
        BasicContextContainer.activityStart(this.getPackageName(), this);
        BasicActivityContainer.switchActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMainActivitySubscription != null && !mMainActivitySubscription.isUnsubscribed()) {
            mMainActivitySubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        long stayTime = System.currentTimeMillis() - mActivityEnterTime;
        releaseLazy();
        super.onDestroy();
    }

    protected void releaseLazy() {
        BasicActivityContainer.recycleBasicContext(this);
    }
}
