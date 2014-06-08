package com.comic.chhreader.detail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentDataDetail;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.FileOperation;
import com.comic.chhreader.utils.SharedPreferencesUtils;
import com.comic.chhreader.utils.Utils;

public class DetailActivity extends Activity {

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

	private boolean mDestroyed = false;
	private boolean mPaused = false;
	private boolean mNoImage = false;

	public List<String> mImgUrls = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int sysVersion = VERSION.SDK_INT;
		if (sysVersion >= 19) {
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}

		mContext = this;

		setContentView(R.layout.detail_activity);

		mCustomWebView = (CustomWebView) findViewById(R.id.content);
		mWebProgress = (ProgressBar) findViewById(R.id.web_progress);
		mLoadingView = (View) findViewById(R.id.web_empty_view);

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
		mCustomWebView.setVisibility(View.GONE);
		mWebProgress.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);

		new LoadContentAsyncTask().execute(mMainUrl);
	}

	void initActionBar() {
		ActionBar actionbar = getActionBar();
		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle(mMainTitle);
			actionbar.setIcon(R.drawable.chh_icon);
			actionbar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
			actionbar.setSplitBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail_main, menu);

		MenuItem item = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		setShareIntent();

		return true;
	}

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
		case R.id.action_back: {
			if (mCustomWebView != null && mCustomWebView.getUrl() != null && mCustomWebView.getUrl().contains("album")) {
				mCustomWebView.loadUrl(mMainUrl);
			} else {
				finish();
			}
		}
			break;
		case R.id.action_refresh: {
			new DeleteLocalPhotoTask().execute();
		}
			break;
		case R.id.action_view_in_browser: {
			Uri uri = Uri.parse(mMainUrl);
			Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(viewIntent);
		}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
		mDestroyed = true;
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

	private void doImageDownload() {
		if (mNoImage) {
			mCustomWebView.loadUrl("javascript:(function(){" + "var objs = document.getElementsByTagName(\"img\"); " + "for(var i=0;i<objs.length;i++)  " + "{" + "    var imgSrc = objs[i].getAttribute(\"src_link\"); " + "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
			return;
		}
		DownloadWebImgTask downloadTask = new DownloadWebImgTask();
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

		downloadTask.execute(urlStrArray);
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

				Loge.d("URL:  " + url);
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

	public class DownloadWebImgTask extends AsyncTask<String, String, Void> {
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			if (mCustomWebView != null && !mPaused)
				mCustomWebView.loadUrl("javascript:(function(){" + "var objs = document.getElementsByTagName(\"img\"); " + "for(var i=0;i<objs.length;i++)  " + "{" + "    var imgSrc = objs[i].getAttribute(\"src_link\"); " + "    var imgOriSrc = objs[i].getAttribute(\"ori_link\"); " + " if(imgOriSrc == \"" + values[0] + "\"){ " + "    objs[i].setAttribute(\"src\",imgSrc);}" + "}" + "})()");
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mCustomWebView != null && !mPaused)
				mCustomWebView.loadUrl("javascript:(function(){" + "var objs = document.getElementsByTagName(\"img\"); " + "for(var i=0;i<objs.length;i++)  " + "{" + "    var imgSrc = objs[i].getAttribute(\"src_link\"); " + "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(String... params) {
			URL url = null;
			InputStream inputStream = null;
			OutputStream outputStream = null;
			HttpURLConnection urlCon = null;

			if (params.length == 0)
				return null;

			File dir = new File(HtmlParser.IMAGE_CACHE_SUB_FOLDER + mThreadId + "/");
			if (!dir.exists()) {
				dir.mkdirs();
			}

			for (String urlStr : params) {
				if (mDestroyed) {
					Loge.d("DownloadWebImgTask Destroyed stop loading AAA");
					return null;
				}
				try {
					if (urlStr == null) {
						break;
					}
					int index = urlStr.lastIndexOf("/");
					String fileName = urlStr.substring(index + 1, urlStr.length());

					File file = new File(HtmlParser.IMAGE_CACHE_SUB_FOLDER + mThreadId + "/" + fileName);

					if (file.exists() && file.length() != 0) {
						publishProgress(urlStr);
						continue;
					}

					if (file.length() == 0) {
						file.delete();
					}

					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}

					url = new URL(urlStr);
					Loge.d("DownloadWebImgTask openConnection A");
					urlCon = (HttpURLConnection) url.openConnection();
					Loge.d("DownloadWebImgTask openConnection B");
					urlCon.setRequestMethod("GET");
					urlCon.setReadTimeout(5000);
					urlCon.setDoInput(true);
					urlCon.connect();

					int contentSize = urlCon.getContentLength();

					inputStream = urlCon.getInputStream();
					outputStream = new FileOutputStream(file);
					byte buffer[] = new byte[100];
					int bufferLength = 0;
					while ((bufferLength = inputStream.read(buffer)) > 0 && !mDestroyed) {
						outputStream.write(buffer, 0, bufferLength);
					}
					outputStream.flush();

					if (file.length() < contentSize || file.length() == 0) {
						file.delete();
					}

					if (mDestroyed) {
						Loge.d("DownloadWebImgTask Destroyed stop loading BBB");
						file.delete();
						urlCon.disconnect();
					}
					publishProgress(urlStr);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
						if (outputStream != null) {
							outputStream.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		}
	}

	private class DeleteLocalPhotoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mThreadId != null && mThreadId.length() > 0) {
				File dir = new File(HtmlParser.IMAGE_CACHE_SUB_FOLDER + mThreadId + "/");
				if (dir.exists()) {
					FileOperation.deleteDirectory(dir);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mMainContent != null && !mMainContent.isEmpty()) {
				mCustomWebView.loadDataWithBaseURL(mMainUrl, mMainContent, "text/html", "utf-8", mMainUrl);
			} else {
				mCustomWebView.loadUrl(mMainUrl);
			}
			super.onPostExecute(result);
		}

	}
}
