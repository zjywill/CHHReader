package com.comic.chhreader.detail;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;

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

		mWebView.setWebChromeClient(new DetailWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(false);

		Intent dataIntent = getIntent();
		mMainTitle = dataIntent.getStringExtra("title");
		mMainUrl = dataIntent.getStringExtra("url");

		initActionBar();
	}

	void initActionBar() {
		ActionBar actionbar = getActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle(mMainTitle);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home :
				finish();
				break;
			default :
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWebView.loadUrl(mMainUrl);
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

	private class DetailWebViewClient extends WebChromeClient {

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
