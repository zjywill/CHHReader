package com.comic.chhreader.detail;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentDataDetail;
import com.comic.chhreader.imageloader.ImageCacheManager;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.SharedPreferencesUtils;
import com.comic.chhreader.utils.Utils;

public class DetailActivity extends Activity {
	private final int DIALOG_PROGRESS = 101;

	private CustomWebView mCustomWebView;
	private ProgressBar mWebProgress;
	private View mLoadingView;

	private String mMainTitle;
	private String mMainUrl;
	private String mMainContent;
	private String mThreadId;

	private Context mContext;

	private ShareActionProvider mShareActionProvider;

	private HtmlParser mParser;

	private boolean mLoadNewsUrl = false;
	private boolean mPaused = false;
	private boolean mNoImage = false;
	private boolean mFavor = false;

	private MenuItem mFavorMenuItem;

	public List<String> mImgUrls = new ArrayList<String>();

	//	private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
	//	private static final int CORE_POOL_SIZE = 8;
	//	private static final int MAXIMUM_POOL_SIZE = 8;
	//	private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
	//
	//	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
	//			MAXIMUM_POOL_SIZE, TimeUnit.SECONDS, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		Intent dataIntent = getIntent();
		mMainTitle = dataIntent.getStringExtra("title");
		mMainUrl = dataIntent.getStringExtra("url");
		mLoadNewsUrl = dataIntent.getBooleanExtra("news", false);
		Loge.d("MainUrl: " + mMainUrl);
		Loge.d("is News: " + mLoadNewsUrl);

		setContentView(R.layout.detail_activity);

		initViews();

		if (!Utils.isNetworkAvailable(getBaseContext())) {
			Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
		}
	}

	private void initViews() {
		mCustomWebView = (CustomWebView) findViewById(R.id.content);
		mWebProgress = (ProgressBar) findViewById(R.id.web_progress);
		mLoadingView = (View) findViewById(R.id.web_empty_view);

		mCustomWebView.setWebChromeClient(new DetailWebChromeClient());
		mCustomWebView.setWebViewClient(new DetailWebViewClient());

		initActionBar();

		if (!mLoadNewsUrl) {
			new LoadContentAsyncTask().execute(mMainUrl);
			mCustomWebView.setVisibility(View.GONE);
			mWebProgress.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);
		} else {
			mCustomWebView.loadUrl(mMainUrl);
			mCustomWebView.setVisibility(View.VISIBLE);
			mWebProgress.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
		}
	}

	private void initActionBar() {
		ActionBar actionbar = getActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle(mMainTitle);
		}
	}

	//
	//	@Override
	//	public void onClick(View v) {
	//		int id = v.getId();
	//		switch (id) {
	//			case ID_MENU_BTN: {
	//				showRotateAnimation();
	//			}
	//				break;
	//			case ID_MENU_BTN_VIEW_ORIGIN: {
	//				Uri uri = Uri.parse(mMainUrl);
	//				Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
	//				startActivity(viewIntent);
	//			}
	//				break;
	//			case ID_MENU_BTN_SHARE: {
	//				shareText(mMainTitle + "    " + mMainUrl);
	//			}
	//				break;
	//			default:
	//				break;
	//		}
	//	}

	private void setShareIntent() {
		if (mMainUrl == null || mMainUrl.isEmpty()) {
			return;
		}
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, mMainTitle + "    " + mMainUrl);
		shareIntent.setType("text/plain");
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(shareIntent);
		}
	}

	private void shareText(String msg) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		try {
			this.startActivity(Intent.createChooser(intent, this.getText(R.string.action_share)));
		} catch (ActivityNotFoundException e) {
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
			case R.id.action_back: {
				if (mCustomWebView != null && mCustomWebView.getUrl() != null
						&& mCustomWebView.getUrl().contains("album")) {
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_PROGRESS:
				ProgressDialog progress = new ProgressDialog(DetailActivity.this);
				progress.setCancelable(false);
				return progress;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DIALOG_PROGRESS:
				((ProgressDialog) dialog).setIndeterminate(true);
				dialog.setCancelable(false);
				((ProgressDialog) dialog).setMessage(getString(R.string.save_to_evernote));
		}
	}

	@Override
	protected void onResume() {
		mPaused = false;
		if (SharedPreferencesUtils.getNoImageMode(mContext) && !Utils.isWifiAvailable(mContext)) {
			mNoImage = true;
		}
		doImageDownload();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mPaused = true;
		super.onPause();
		mCustomWebView.stopLoading();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mCustomWebView.destroy();
		mCustomWebView = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

		@Override
		public void onPageFinished(WebView view, String url) {
			if (mMainContent != null && !mMainContent.isEmpty()) {
				doImageDownload();
			}
		}
	}

	private class DetailWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
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
				mFavor = DataBaseUtils.getContentFavorData(mContext, url);

				mThreadId = HtmlParser.getThreadId(url);

				ContentDataDetail contentData = DataBaseUtils.getContentData(mContext, url);

				if (contentData == null) {
					return null;
				}

				if (contentData.mImageSet == null || contentData.mImageSet.isEmpty()) {
					return null;
				}

				String[] imageSet = contentData.mImageSet.split(HtmlParser.IMAGE_BREAK_TAG);

				for (String imageurl : imageSet) {
					mImgUrls.add(imageurl);
				}

				if (contentData != null && contentData.mBody != null && !contentData.mBody.isEmpty()) {
					long timeGap = System.currentTimeMillis() - contentData.mUpdateDate;
					if (timeGap < DateUtils.DAY_IN_MILLIS) {
						return contentData.mBody;
					}
				}

				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && result.length() > 0 && !result.equals("fail")) {
				mMainContent = result;
				if (mCustomWebView == null) {
					return;
				}
				mCustomWebView.setVisibility(View.VISIBLE);
				mCustomWebView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
				mLoadingView.setVisibility(View.GONE);

				if (mFavor && mFavorMenuItem != null) {
					Loge.d("Favor B: " + mFavor);
					mFavorMenuItem.setIcon(R.drawable.ic_menu_favor_active);
				}
			} else {
				if (mParser == null) {
					mParser = new HtmlParser(mContext, mMainUrl) {
						@Override
						protected String handleDocument(Document doc) {
							return doc.html();
						}

						@Override
						protected void excuteEnd(String result) {
							if (result != null && result.length() > 0) {
								if (mCustomWebView == null) {
									return;
								}
								mMainContent = result;
								mCustomWebView.setVisibility(View.VISIBLE);
								mWebProgress.setVisibility(View.VISIBLE);
								mCustomWebView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
								mLoadingView.setVisibility(View.GONE);
							} else {
								if (mCustomWebView == null) {
									return;
								}
								mLoadingView.setVisibility(View.GONE);
								mCustomWebView.setVisibility(View.VISIBLE);
								mWebProgress.setVisibility(View.VISIBLE);
								mCustomWebView.loadUrl(mMainUrl);
							}
						}
					};
					mParser.execute();
				}
			}
			setShareIntent();
			super.onPostExecute(result);
		}
	}

	private void doImageDownload() {
		if (mNoImage) {
			mCustomWebView.loadUrl("javascript:(function(){"
					+ "var objs = document.getElementsByTagName(\"img\"); "
					+ "for(var i=0;i<objs.length;i++)  " + "{"
					+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
					+ "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
			return;
		}
		if (mImgUrls.isEmpty()) {
			if (mParser != null) {
				mImgUrls.addAll(mParser.getImgUrls());
			}
		}
		if (mImgUrls.isEmpty()) {
			return;
		}
		String urlStrArray[] = new String[mImgUrls.size() + 1];
		mImgUrls.toArray(urlStrArray);

		for (String urlStr : urlStrArray) {
			if (urlStr == null) {
				break;
			}
			new DownloadWebImgTask(urlStr).execute();
		}
	}

	public class DownloadWebImgTask extends AsyncTask<Void, Void, String> {

		private String mUrl;

		public DownloadWebImgTask(String url) {
			mUrl = url;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (ImageCacheManager.getInstance().getBitmap(mUrl) != null) {
				return "cached";
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				ImageCacheManager.getInstance().getImage(mUrl, new ImageListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Loge.d("DetailActivity onErrorResponse");
					}

					@Override
					public void onResponse(ImageContainer arg0, boolean arg1) {
						if (mCustomWebView != null && !mPaused) {
							Loge.d("DetailActivity onResponse url: " + arg0.getRequestUrl());
							if (mCustomWebView != null && !mPaused) {
								mCustomWebView.loadUrl("javascript:(function(){"
										+ "var objs = document.getElementsByTagName(\"img\"); "
										+ "for(var i=0;i<objs.length;i++)  " + "{"
										+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
										+ "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
							}
						}

					}
				});
			} else {
				if (mCustomWebView != null && !mPaused) {
					mCustomWebView.loadUrl("javascript:(function(){"
							+ "var objs = document.getElementsByTagName(\"img\"); "
							+ "for(var i=0;i<objs.length;i++)  " + "{"
							+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
							+ "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
				}
			}
		}

	}

	private class DeleteLocalPhotoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mThreadId != null && mThreadId.length() > 0) {
				//				File dir = new File(HtmlParser.IMAGE_CACHE_SUB_FOLDER + mThreadId + "/");
				//				if (dir.exists()) {
				//					FileOperation.deleteDirectory(dir);
				//				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mCustomWebView == null) {
				return;
			}
			if (mMainContent != null && !mMainContent.isEmpty()) {
				mCustomWebView.loadDataWithBaseURL(mMainUrl, mMainContent, "text/html", "utf-8", mMainUrl);
			} else {
				mCustomWebView.loadUrl(mMainUrl);
			}
			super.onPostExecute(result);
		}

	}
}
