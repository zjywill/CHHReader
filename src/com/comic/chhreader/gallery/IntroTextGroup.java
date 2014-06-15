package com.comic.chhreader.gallery;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class IntroTextGroup extends ViewGroup {

	private int mPageCount = 0;

	private List<CustomGalleryTextView> mTexViewList;

	private Paint mTextPaint;

	private int mViewWidth;
	private int mViewHeight;

	private int mFormerPos = 0;

	private GalleryRootView mRootView;

	public IntroTextGroup(Context context, GalleryRootView rootView,
			int pageCount) {
		super(context);
		mRootView = rootView;
		mPageCount = pageCount;
		initView();
	}

	public IntroTextGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		mTextPaint = new Paint();
		mTextPaint.setTextSize(20);

		mTexViewList = new ArrayList<CustomGalleryTextView>(mPageCount);
		for (int i = 0; i < mPageCount; i++) {
			CustomGalleryTextView textView = new CustomGalleryTextView(getContext());
			mTexViewList.add(textView);
			addView(textView);
		}
	}

	public void swepToPistion(float offsetPercent) {
		int offsetX = (int) (-offsetPercent * mViewWidth);
		if (mFormerPos != offsetX) {
			scrollTo(offsetX, 0);
			mFormerPos = offsetX;
		}
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width = r - l;
		int height = b - t;

		for (int i = 0, size = getChildCount(); i < size; i++) {
			View textview = (View) getChildAt(i);
			int leftPadding = (width - textview.getMeasuredWidth()) / 2;
			int topPadding = (height - textview.getMeasuredHeight()) / 2;
			textview.layout(leftPadding + width * i, topPadding, leftPadding + width * (i + 1) + width, topPadding + textview.getMeasuredHeight());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mViewWidth = getDefaultSize(0, widthMeasureSpec);
		mViewHeight = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(mViewWidth, mViewHeight);

		for (int i = 0, size = getChildCount(); i < size; i++) {
			View textview = (View) getChildAt(i);
			textview.measure(mViewWidth, mViewHeight);
		}
	}

	@Override
	public void invalidate() {
		if (mTexViewList != null && mTexViewList.size() > 0) {
			if (mRootView.mENews.size() > 5) {
				for (int i = 0; i < mTexViewList.size(); i++) {
					mTexViewList.get(i).setText(mRootView.mENews.get(i).title);
				}
			}
		}
		super.invalidate();
	}
}
