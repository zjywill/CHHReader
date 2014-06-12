package com.comic.chhreader.gallery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
		Loge.d("CustomGalleryTextView init");
		mText = "gfdgfdhkjjhjhkjhkjhkjhkjhjh";
		mTextSize = Utils.dipToPx(getContext(), INITIAL_TEXT_SIZE);

		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(mTextSize);

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Paint.Style.FILL);
		mBackgroundPaint.setColor(Color.RED);
	}

	public void setText(String text) {
		//		mText = text;
		//		invalidate();
	}

	public void setTextSize(int sizedp) {
		//		mTextSize = Utils.dipToPx(getContext(), sizedp);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mWidth = widthMeasureSpec;
		mHeight = heightMeasureSpec;
		setMeasuredDimension(mWidth, mWidth);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Loge.d("CustomGalleryTextView onDraw");
		Loge.d("CustomGalleryTextView onDraw mTextSize: " + mTextSize);
		Loge.d("CustomGalleryTextView onDraw mText: " + mText);
		//		canvas.drawColor(Color.DKGRAY);
		canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mBackgroundPaint);
		canvas.drawText(mText, getLeft(), getTop() + mTextSize, mTextPaint);
	}

}
