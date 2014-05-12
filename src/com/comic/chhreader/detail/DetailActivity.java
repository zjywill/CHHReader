package com.comic.chhreader.detail;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.utils.Utils;

public class DetailActivity extends Activity {

	private WebView mWebView;
	private ProgressBar mWebProgress;

	private String mMainTitle;
	private String mMainUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_activity);

		mWebView = (WebView) findViewById(R.id.detail_web);
		mWebProgress = (ProgressBar) findViewById(R.id.web_progress);

		mWebView.setWebChromeClient(new DetailWebChromeClient());
		mWebView.setWebViewClient(new DetailWebViewClient());
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		int screenDensity = getResources().getDisplayMetrics().densityDpi ;   
		WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM ;   
		switch (screenDensity){   
		case DisplayMetrics.DENSITY_LOW :  
		    zoomDensity = WebSettings.ZoomDensity.CLOSE;  
		    break;  
		case DisplayMetrics.DENSITY_MEDIUM:  
		    zoomDensity = WebSettings.ZoomDensity.MEDIUM;  
		    break;  
		case DisplayMetrics.DENSITY_HIGH:  
		    zoomDensity = WebSettings.ZoomDensity.FAR;  
		    break ;  
		}  
		webSettings.setDefaultZoom(zoomDensity); 
		
		webSettings.setSupportZoom(false);
		
		Intent dataIntent = getIntent();
		mMainTitle = dataIntent.getStringExtra("title");
		mMainUrl = dataIntent.getStringExtra("url");

		Loge.d("MainUrl: " + mMainUrl);

		initActionBar();
		if (!Utils.isNetworkAvailable(getBaseContext())) {
			Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
		}
		mWebView.loadUrl(mMainUrl);
		//mWebView.loadUrl("file:///android_asset/test.html");
	}

	void initActionBar() {
		ActionBar actionbar = getActionBar();
		if (actionbar != null) {
			actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle(mMainTitle);
			actionbar.setIcon(R.drawable.chh_icon);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			if (mWebView.getUrl().contains("album")) {
				mWebView.loadUrl(mMainUrl);
			} else {
				finish();
			}
		}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWebView.stopLoading();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
	}

	@Override
	public void onBackPressed() {
		String url = mWebView.getUrl();
		Loge.i("onBackPressed url =" + url);
		if (url.contains("album")) {
			mWebView.loadUrl(mMainUrl);
		} else {
			finish();
		}
	}

	private class DetailWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Loge.i("new url = " + url);
			if (url.contains("album")) {
				return false;
			} else if (url.contains("thread-") || url.contains("article-")) {
				return false;
			}
			Toast.makeText(getBaseContext(), R.string.not_support, Toast.LENGTH_SHORT).show();
			return true;
		}
	}

	private class DetailWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			Loge.i("Load Web progress = " + newProgress);
			mWebProgress.setProgress(newProgress);
			if (newProgress == 100) {
				mWebProgress.setVisibility(View.GONE);
			}
		}

	}
}
