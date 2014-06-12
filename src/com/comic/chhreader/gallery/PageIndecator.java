package com.comic.chhreader.gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.comic.chhreader.R;

public class PageIndecator extends HorizontalScrollView {

	private int mPageCount = 0;
	private int mPosition = 0;

	private LinearLayout mLinearLayout;

	public PageIndecator(Context context, int pageCount) {
		super(context);
		mPageCount = pageCount;
		initView();
	}

	public PageIndecator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		mLinearLayout = new LinearLayout(getContext());
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		addView(mLinearLayout, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
		setIndecatorPostion(mPosition);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	public void setIndecatorPostion(int postion) {
		mPosition = postion;
		if (mPageCount > 0) {
			mLinearLayout.setVisibility(View.VISIBLE);
			mLinearLayout.removeAllViews();
			for (int i = 0; i < mPageCount; i++) {
				ImageView indecatorItem = new ImageView(getContext());
				if (i == mPosition) {
					indecatorItem.setImageResource(R.drawable.indecator_select);
				} else {
					indecatorItem.setImageResource(R.drawable.indecator_normal);
				}
				mLinearLayout.addView(indecatorItem, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}
		} else {
			mLinearLayout.setVisibility(View.GONE);
		}
	}
}
