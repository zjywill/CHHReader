package com.comic.chhreader.gallery;

import com.comic.chhreader.utils.Utils;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class GalleryRootView extends FrameLayout {

	public static final int GALLERY_HEIGHT = 180;

	private Interpolator mInterpolator = new IntroInterpolator();
	private Scroller mScroller = null;

	private PageIndecator mPageIndecator = null;
	private IntroTextGroup mIntroTextGroup = null;

	private PointF mStartPoint;
	private PointF mCurrentPoint;
	private VelocityTracker mVelocityTracker;
	private CenterCorssFadeView mCenterCorssFadeView;

	private int mHeight = 0;
	private int mViewWidth = 0;
	private int mPageCount = 5;
	private int mPageCurrent = 0;
	private int mUpDistance = 0;

	private boolean onAnimation = false;
	private boolean bounceBack = false;

	public GalleryRootView(Context context) {
		super(context);
		initView();
	}

	public GalleryRootView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		mHeight = Utils.dipToPx(getContext(), GALLERY_HEIGHT);

		mScroller = new Scroller(getContext(), mInterpolator);

		mIntroTextGroup = new IntroTextGroup(getContext(), mPageCount, 0);
		FrameLayout.LayoutParams introTextParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, mHeight / 3);
		introTextParams.gravity = Gravity.BOTTOM;
		mIntroTextGroup.setLayoutParams(introTextParams);

		mCenterCorssFadeView = new CenterCorssFadeView(getContext(), mPageCount);
		FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, mHeight);
		mCenterCorssFadeView.setLayoutParams(centerParams);

		mPageIndecator = new PageIndecator(getContext(), mPageCount);
		FrameLayout.LayoutParams indecatorParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		indecatorParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
		mPageIndecator.setLayoutParams(indecatorParams);

		addView(mCenterCorssFadeView);
		addView(mIntroTextGroup);
		addView(mPageIndecator);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (onAnimation) {
			return false;
		}

		int action = event.getAction();

		if (mStartPoint == null) {
			mStartPoint = new PointF();
			mStartPoint.set(event.getX(), event.getY());
		}

		if (mCurrentPoint == null) {
			mCurrentPoint = new PointF();
		}
		mCurrentPoint.set(event.getX(), event.getY());

		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				if (mVelocityTracker == null) {
					mVelocityTracker = VelocityTracker.obtain();
				}
				mVelocityTracker.addMovement(event);
			}
				break;
			case MotionEvent.ACTION_MOVE: {
				mVelocityTracker.addMovement(event);
				//				int yMove = (int) (mCurrentPoint.y - mStartPoint.y);
				//				if (yMove > 0) {
				//					return false;
				//				}
				int xMove = (int) (mCurrentPoint.x - mStartPoint.x);
				takeMove(xMove - mViewWidth * mPageCurrent);
			}
				break;
			case MotionEvent.ACTION_UP: {
				mVelocityTracker.addMovement(event);
				moveToNextPage();
				mVelocityTracker.recycle();
				mVelocityTracker = null;
				mStartPoint = null;
				return true;
			}
			case MotionEvent.ACTION_CANCEL: {
				return true;
			}
			default:
				break;
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
		super.onMeasure(widthMeasureSpec, mHeight);
	}

	private void moveToNextPage() {
		int xMove = (int) (mCurrentPoint.x - mStartPoint.x);
		float movePercent = xMove / (1.0F * mViewWidth);
		mVelocityTracker.computeCurrentVelocity(1000);
		int moveXSpeed = (int) mVelocityTracker.getXVelocity();

		mUpDistance = xMove;

		if (mUpDistance == 0) {
			return;
		}

		if (Math.abs(movePercent) > 0.1f || Math.abs(moveXSpeed) > 0) {
			bounceBack = false;

			int pagenow = 0;
			if (mUpDistance < 0) {
				if (mPageCurrent < mPageCount)
					pagenow = mPageCurrent + 1;
			} else {
				if (mPageCurrent > 0)
					pagenow = mPageCurrent - 1;
			}

			if (pagenow == mPageCount) {
				return;
			}

			mPageIndecator.setIndecatorPostion(pagenow);

			if (mPageCurrent == 0 && mUpDistance > 0) {
				return;
				//scrollToNext(0, mUpDistance);
			} else {
				int remainX = mViewWidth - Math.abs(xMove);
				scrollToNext(0, remainX);
			}
		} else {
			bounceBack = true;
			if (mUpDistance < 0) {
				scrollToNext(0, mUpDistance);
			} else {
				scrollToNext(0, -mUpDistance);
			}
		}
	}

	private void takeMove(int moveX) {
		if ((mPageCurrent == 0 && moveX > 0)
				|| (moveX < (-mViewWidth * (mPageCount - 1)) && mPageCurrent == (mPageCount - 1))) {
			return;
		}
		float movePercent = moveX / (1.0F * mViewWidth);
		mIntroTextGroup.swepToPistion(movePercent);
		mCenterCorssFadeView.swepToFade(movePercent);
	}

	private void scrollToNext(int start, int finish) {
		int end = finish - start;
		mScroller.startScroll(start, 0, end, 0, 300);
		mScroller.setFinalX(finish);
		onAnimation = true;
		post(new AnimateRunnable());
	}

	private class AnimateRunnable implements Runnable {
		@Override
		public void run() {
			if (mScroller.isFinished()) {
				onAnimation = false;
			}

			while (!mScroller.computeScrollOffset()) {
				if (!bounceBack) {
					if (mUpDistance < 0) {
						if (mPageCurrent < mPageCount)
							mPageCurrent++;
					} else {
						if (mPageCurrent > 0)
							mPageCurrent--;
					}
				}
				return;
			}

			int distanceX = 0;
			if (mUpDistance < 0) {
				distanceX = mUpDistance - mScroller.getCurrX();
				if (mPageCurrent > 0) {
					distanceX -= (mPageCurrent * mViewWidth);
				}
			} else {
				distanceX = mUpDistance + mScroller.getCurrX() - mViewWidth;
				if (mPageCurrent > 0) {
					distanceX -= ((mPageCurrent - 1) * mViewWidth);
				}
			}

			if (mUpDistance > 0 && mPageCurrent == 0) {
				distanceX = mUpDistance - mScroller.getCurrX();
			}

			takeMove(distanceX);
			post(new AnimateRunnable());
		}
	};
}
