package com.comic.chhreader.gallery;

import com.comic.chhreader.R;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PageOneView extends RelativeLayout {

	private ImageView mImageView;

	public PageOneView(Context context) {
		super(context);
		initView();
	}

	private void initView() {
		mImageView = new ImageView(getContext());
		mImageView.setImageResource(R.drawable.ic_launcher);
		mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(mImageView, imageParams);
	}

	public void setImage(int resId) {
		if (mImageView != null) {
			mImageView.setImageResource(resId);
		}
	}
}
