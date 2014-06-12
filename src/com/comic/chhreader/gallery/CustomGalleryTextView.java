package com.comic.chhreader.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.view.View;

import com.comic.chhreader.Loge;
import com.comic.chhreader.utils.Utils;

public class CustomGalleryTextView extends View {

	private static final int INITIAL_TEXT_SIZE = 20;

	private String mText;
	private int mTextSize;
	private Paint mTextPaint;

	private int mBackgroundColor;
	private Paint mBackgroundPaint;

	private int mHeight;
	private int mWidth;

	public CustomGalleryTextView(Context context) {
		super(context);
		setWillNotDraw(false);
		init();
	}

	private void init() {
		mTextSize = Utils.dipToPx(getContext(), INITIAL_TEXT_SIZE);

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
		canvas.drawText(mText, 0, mTextSize, mTextPaint);
	}

}
