package com.comic.chhreader.gallery;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.image.PhotoView;

@SuppressLint("NewApi")
public class CenterCorssFadeView extends FrameLayout {

	private int mPageCount;

	private float mFormerAlpha = 0;

	private List<PhotoView> mCenterLayoutList = null;

	private GalleryRootView mRootView;

	private Drawable mLogoDrawable;

	public CenterCorssFadeView(Context context, GalleryRootView rootView,
			int pagecount) {
		super(context);
		mPageCount = pagecount;
		mRootView = rootView;
		initView();
	}

	private void initView() {
		mLogoDrawable = getContext().getResources().getDrawable(R.drawable.feedlogo);
		if (mCenterLayoutList == null) {
			mCenterLayoutList = new ArrayList<PhotoView>(mPageCount);
		}

		FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

		for (int i = 0; i < 5; i++) {
			PhotoView view = new PhotoView(getContext());
			mCenterLayoutList.add(view);
			addView(view, centerParams);
			if (i > 0) {
				view.setVisibility(View.GONE);
			}
		}
	}

	public void swepToFade(float offsetPercent) {
		int currentPage = (int) (Math.abs(offsetPercent / 1));
		float alpha = 1 + offsetPercent + currentPage;
		if (alpha >= 0 && alpha < 1) {
			if (mFormerAlpha != alpha && currentPage < (mPageCount - 1)) {
				View currentView = mCenterLayoutList.get(currentPage);
				View nextView = mCenterLayoutList.get(currentPage + 1);
				if (currentView != null && nextView != null) {
					if (alpha > 0.9f && nextView.getVisibility() == View.GONE) {
						nextView.setVisibility(View.VISIBLE);
					}
					if (alpha == 0 && currentView.getVisibility() == View.VISIBLE) {
						currentView.setVisibility(View.GONE);
					}
					currentView.setAlpha((alpha - 0.5f) * 2);
					nextView.setAlpha(1 - alpha);
				}
				mFormerAlpha = alpha;
			}
		}
	}

	@Override
	public void invalidate() {
		if (mCenterLayoutList != null && mCenterLayoutList.size() > 0) {
			if (mRootView.mENews.size() > 5) {
				for (int i = 0; i < mCenterLayoutList.size(); i++) {
					String imageUrl = mRootView.mENews.get(i).imageurl;
					Loge.d("CenterCorssFadeView invalidate: " + imageUrl);
					try {
						mCenterLayoutList.get(i).setImageURL(new URL(imageUrl), true, true, true, mLogoDrawable);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		super.invalidate();
	}
}
