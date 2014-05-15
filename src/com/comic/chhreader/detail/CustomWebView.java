package com.comic.chhreader.detail;

import android.content.Context;
import android.graphics.PointF;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

public class CustomWebView extends WebView implements View.OnSystemUiVisibilityChangeListener {

	private TextView mTitleView;

	boolean mNavVisible;
	int mBaseSystemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE;
	int mLastSystemUiVis;

	private boolean isScroll;
	private PointF startPoint = new PointF();
	private GestureDetector mGesture = null;

	Runnable mNavHider = new Runnable() {
		@Override
		public void run() {
			setNavVisibility(false);
		}
	};

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGesture = new GestureDetector(getContext(), new GestureListener());
		setClickable(false);

		WebSettings webSettings = getSettings();
		webSettings.setJavaScriptEnabled(false);
		webSettings.setAppCacheEnabled(true);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setSupportZoom(false);
		webSettings.setSaveFormData(true);

		setOnSystemUiVisibilityChangeListener(this);

		setNavVisibility(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mGesture.onTouchEvent(ev);
		return super.onTouchEvent(ev);
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		// Detect when we go out of low-profile mode, to also go out
		// of full screen. We only do this when the low profile mode
		// is changing from its last state, and turning off.
		int diff = mLastSystemUiVis ^ visibility;
		mLastSystemUiVis = visibility;
		if ((diff & SYSTEM_UI_FLAG_LOW_PROFILE) != 0 && (visibility & SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
			setNavVisibility(true);
		}
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);

		// When we become visible, we show our navigation elements briefly
		// before hiding them.
		setNavVisibility(true);
		getHandler().postDelayed(mNavHider, 2000);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		// When the user scrolls, we hide navigation elements.
		setNavVisibility(false);
	}

	void setBaseSystemUiVisibility(int visibility) {
		mBaseSystemUiVisibility = visibility;
	}

	void setNavVisibility(boolean visible) {
		int sysVersion = VERSION.SDK_INT;
		if (sysVersion < 19) {
			return;
		}

		int newVis = mBaseSystemUiVisibility;
		if (!visible) {
			newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE;
		}
		final boolean changed = newVis == getSystemUiVisibility();
		if (changed || visible) {
			Handler h = getHandler();
			if (h != null) {
				h.removeCallbacks(mNavHider);
			}
		}
		setSystemUiVisibility(newVis);
	}

	class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			setNavVisibility(false);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			setNavVisibility(false);
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			int curVis = getSystemUiVisibility();
			setNavVisibility((curVis & SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
			return super.onSingleTapConfirmed(e);
		}
	}
}
