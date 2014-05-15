package com.comic.chhreader.detail;

import android.content.Context;
import android.graphics.PointF;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.comic.chhreader.Loge;

public class CustomWebView extends WebView implements View.OnSystemUiVisibilityChangeListener {

	private TextView mTitleView;

	boolean mNavVisible;
	int mBaseSystemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE;
	int mLastSystemUiVis;

	private boolean isScroll;
	private PointF startPoint = new PointF();

	Runnable mNavHider = new Runnable() {
		@Override
		public void run() {
			setNavVisibility(false);
		}
	};

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);

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
		int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				isScroll = false;
				startPoint.set(ev.getX(), ev.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				float dx = ev.getX() - startPoint.x;
				if (Math.abs(dx) > 2) {
					isScroll = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (!isScroll) {
					int curVis = getSystemUiVisibility();
					setNavVisibility((curVis & SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
				}
				isScroll = false;
				break;
			default:
				break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		// Detect when we go out of low-profile mode, to also go out
		// of full screen.  We only do this when the low profile mode
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
            newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN
                    | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE;
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
}
