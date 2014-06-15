package com.comic.chhreader.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.comic.chhreader.utils.Utils;

public class CustomGalleryTextView extends View {

	private static final int INITIAL_TEXT_SIZE = 20;
	private static final int PADDING = 5;

	private String mText;
	private int mTextSize;
	private Paint mTextPaint;

	private Paint mBackgroundPaint;

	private int mHeight;
	private int mWidth;

	private int mPadding;

	public CustomGalleryTextView(Context context) {
		super(context);
		setWillNotDraw(false);
		init();
	}

	private void init() {
		mTextSize = Utils.dipToPx(getContext(), INITIAL_TEXT_SIZE);
		mPadding = Utils.dipToPx(getContext(), PADDING);

		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(mTextSize);

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Paint.Style.FILL);
		mBackgroundPaint.setColor(Color.BLACK);
		mBackgroundPaint.setAlpha(120);
	}

	public void setText(String text) {
		mText = text;
		invalidate();
	}

	public void setTextSize(int sizedp) {
		mTextSize = Utils.dipToPx(getContext(), sizedp);
		mTextPaint.setTextSize(mTextSize);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidth = widthMeasureSpec;
		mHeight = heightMeasureSpec;
		setMeasuredDimension(mWidth, mHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, mWidth, mHeight, mBackgroundPaint);

		if (mText == null) {
			return;
		}

		int textCount = mTextPaint.breakText(mText, true, mWidth - mPadding / 2, null);
		if (textCount < mText.length()) {
			canvas.drawText(mText.substring(0, textCount - 1), mPadding, mTextSize, mTextPaint);
			String secondText = mText.substring(textCount - 1);
			int secondTextCount = mTextPaint.breakText(secondText, true, mWidth - mPadding / 2, null);
			if (secondTextCount < secondText.length()) {
				secondText = secondText.substring(0, secondTextCount - 5);
				secondText = secondText + " ...";
				canvas.drawText(secondText, mPadding, mTextSize * 2 + mPadding, mTextPaint);
			} else {
				canvas.drawText(secondText.substring(0, secondTextCount), mPadding, mTextSize * 2 + mPadding, mTextPaint);
			}
		} else {
			canvas.drawText(mText, mPadding, mTextSize, mTextPaint);
		}
	}
}
