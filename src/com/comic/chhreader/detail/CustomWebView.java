package com.comic.chhreader.detail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import com.comic.chhreader.Loge;

@SuppressLint("SetJavaScriptEnabled")
public class CustomWebView extends WebView implements View.OnSystemUiVisibilityChangeListener {

	boolean mNavVisible;
	int mBaseSystemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE;
	int mLastSystemUiVis;

	private GestureDetector mGesture = null;

	private String mNightJs;

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
		String appCacheDir = context.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
		webSettings.setAppCachePath(appCacheDir);
		webSettings.setAppCacheEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setSupportZoom(false);
		webSettings.setSaveFormData(false);
		webSettings.setLoadsImagesAutomatically(true);

		addJavascriptInterface(new Js2JavaInterface(), HtmlParser.Js2JavaInterfaceName);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setJavaScriptEnabled(true);

		setOnSystemUiVisibilityChangeListener(this);

		setNavVisibility(true);
	}

	public class Js2JavaInterface {
		private Context context;
		public void setImgSrc(String imgSrc) {
			Loge.i("setImgSrc : " + imgSrc);
		}
	}

	public void injectNightCss() {
		injectNightCss(this);
	}

	private void injectNightCss(WebView view) {
		if (mNightJs == null || mNightJs.isEmpty()) {
			mNightJs = getFromAsset(view.getContext(), "night.js");
		}
		view.loadUrl("javascript:" + mNightJs);
	}

	public static String getFromAsset(Context context, String fileName) {
		String result = "";
		try {
			InputStream in = context.getResources().getAssets().open(fileName);
			int length = in.available();
			byte[] buffer = new byte[length];
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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
			newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| SYSTEM_UI_FLAG_IMMERSIVE;
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
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			setNavVisibility(false);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
