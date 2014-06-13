package com.comic.chhreader.gallery;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;

@SuppressLint("NewApi")
public class CenterCorssFadeView extends FrameLayout{

	private int mPageCount;

	private float mFormerAlpha = 0;

	private List<RelativeLayout> mCenterLayoutList = null;

	View mCurrentView = null;
	View mNextView = null;

	public CenterCorssFadeView(Context context, int pagecount) {
		super(context);
		mPageCount = pagecount;
		initView();
	}

	private void initView() {
		if (mCenterLayoutList == null) {
			mCenterLayoutList = new ArrayList<RelativeLayout>(mPageCount);
		}

		PageOneView view1 = new PageOneView(getContext());
		mCenterLayoutList.add(view1);

		PageOneView view2 = new PageOneView(getContext());
		mCenterLayoutList.add(view2);

		PageOneView view3 = new PageOneView(getContext());
		mCenterLayoutList.add(view3);

		PageOneView view4 = new PageOneView(getContext());
		mCenterLayoutList.add(view4);

		PageOneView view5 = new PageOneView(getContext());
		mCenterLayoutList.add(view5);
		
		view1.setImage(R.drawable.news_01);
		view2.setImage(R.drawable.news_02);
		view3.setImage(R.drawable.news_03);
		view4.setImage(R.drawable.news_04);
		view5.setImage(R.drawable.news_05);

		FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

		addView(view1, centerParams);
		addView(view2, centerParams);
		addView(view3, centerParams);
		addView(view4, centerParams);
		addView(view5, centerParams);

		view2.setVisibility(View.GONE);
		view3.setVisibility(View.GONE);
		view4.setVisibility(View.GONE);
		view5.setVisibility(View.GONE);
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

}
