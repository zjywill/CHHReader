package com.comic.chhreader.detail;

import java.io.InputStream;

import org.apache.http.util.EncodingUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentDataDetail;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.Utils;

public class DetailActivity extends Activity {

	private CustomWebView mCustomWebView;
	private ProgressBar mWebProgress;

	private String mMainTitle;
	private String mMainUrl;

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		mContext = this;

		setContentView(R.layout.detail_activity);

		mCustomWebView = (CustomWebView) findViewById(R.id.content);
		mWebProgress = (ProgressBar) findViewById(R.id.web_progress);

		mCustomWebView.setWebChromeClient(new DetailWebChromeClient());
		mCustomWebView.setWebViewClient(new DetailWebViewClient());

		Intent dataIntent = getIntent();
		mMainTitle = dataIntent.getStringExtra("title");
		mMainUrl = dataIntent.getStringExtra("url");

		Loge.d("MainUrl: " + mMainUrl);

		initActionBar();
		if (!Utils.isNetworkAvailable(getBaseContext())) {
			Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
		}
		new LoadContentAsyncTask().execute(mMainUrl);
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
			if (mCustomWebView != null && mCustomWebView.getUrl() != null && mCustomWebView.getUrl().contains("album")) {
				mCustomWebView.loadUrl(mMainUrl);
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
		mCustomWebView.stopLoading();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCustomWebView.destroy();
	}

	@Override
	public void onBackPressed() {
		String url = mCustomWebView.getUrl();
		Loge.i("onBackPressed url =" + url);
		if (url != null && url.contains("album")) {
			mCustomWebView.loadUrl(mMainUrl);
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

	class LoadContentAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			if (params == null) {
				return "fail";
			} else {
				String url = "";
				if (params.length > 0) {
					url = params[0];
				}
				if (url.isEmpty()) {
					return "fail";
				}

				Loge.d("URL:  " + url);

				ContentDataDetail contentData = DataBaseUtils.getContentData(mContext, url);

				if (contentData != null) {
					long timeGap = System.currentTimeMillis() - contentData.mUpdateDate;
					if (timeGap < DateUtils.DAY_IN_MILLIS) {
						return contentData.mBody;
					}
				}

				String body = CHHNetUtils.getContentBody(mContext, url);

				if (body.isEmpty()) {
					return "fail";
				}

				String result = "";
				try {
					InputStream in = getResources().getAssets().open("head.html");
					int lenght = in.available();
					byte[] buffer = new byte[lenght];
					in.read(buffer);
					result = EncodingUtils.getString(buffer, "utf-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = result + body;

				DataBaseUtils.updateContentData(mContext, url, result);

				return result;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.length() > 0 && !result.equals("fail")) {
				mCustomWebView.loadDataWithBaseURL(mMainUrl, result, "text/html", "utf-8", null);
			} else {
				mCustomWebView.loadUrl(mMainUrl);
			}
			super.onPostExecute(result);
		}

	}
}
