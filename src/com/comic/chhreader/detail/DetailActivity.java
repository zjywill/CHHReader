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
import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.comic.chhreader.evernoteshare.ShareToEvernote;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.FileOperation;
import com.comic.chhreader.utils.SharedPreferencesUtils;
import com.comic.chhreader.utils.Utils;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;

public class DetailActivity extends Activity {

	protected final int DIALOG_PROGRESS = 101;

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
	private boolean mDestroyed = false;
	private boolean mPaused = false;
	private boolean mNoImage = false;
	private boolean mFavor = false;

	private MenuItem mFavorMenuItem;

	public List<String> mImgUrls = new ArrayList<String>();

	private OnClientCallback<Note> mNoteCreateCallback = new OnClientCallback<Note>() {
		@Override
		public void onSuccess(Note note) {
			Toast.makeText(getApplicationContext(), R.string.note_saved, Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}

		@Override
		public void onException(Exception exception) {
			Toast.makeText(getApplicationContext(), R.string.note_saved, Toast.LENGTH_LONG).show();
			Loge.e("NoteCreateCallback onException: " + exception.getMessage());
			removeDialog(DIALOG_PROGRESS);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Feature For FULL SCREEN MODE
//		int sysVersion = VERSION.SDK_INT;
//		if (sysVersion >= 19) {
//			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//		}

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

		mFavorMenuItem = menu.findItem(R.id.action_favor);
		if (mFavor) {
			Loge.d("Favor A: " + mFavor);
			mFavorMenuItem.setIcon(R.drawable.ic_menu_favor_active);
		} else {
			mFavorMenuItem.setIcon(R.drawable.ic_menu_favor);
		}

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
				if (mCustomWebView != null && mCustomWebView.getUrl() != null
						&& mCustomWebView.getUrl().contains("album")) {
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
			case R.id.action_evernote: {
				if (!ShareToEvernote.getInstance(this).isLoggedIn()) {
					Loge.d("App not Logged In");
					ShareToEvernote.getInstance(this).authenticate();
				} else {
					Loge.i("App Logged In");
					if (!ShareToEvernote.getInstance(this).isAppLinkedNotebook()) {
						saveToEvernote();
					}
				}
			}
				break;
			case R.id.action_favor: {
				mFavor = DataBaseUtils.getContentFavorData(mContext, mMainUrl);
				DataBaseUtils.updateContentFavorData(mContext, mMainUrl, !mFavor ? 1 : 0);
				mFavor = !mFavor;
				if (mFavor) {
					item.setIcon(R.drawable.ic_menu_favor_active);
				} else {
					item.setIcon(R.drawable.ic_menu_favor);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case EvernoteSession.REQUEST_CODE_OAUTH:
				if (resultCode == Activity.RESULT_OK) {
					Loge.i("App Logged In Success");
					if (!ShareToEvernote.getInstance(this).isAppLinkedNotebook()) {
						saveToEvernote();
					}
				}
				break;
		}
	}

	private void saveToEvernote() {
		if ((mMainContent != null && !mMainContent.isEmpty()) || mLoadNewsUrl) {
			showDialog(DIALOG_PROGRESS);
			String content = "";
			if (!mLoadNewsUrl) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						String content = DataBaseUtils.getContentOriginData(mContext, mMainUrl);
						content = content.replaceAll("b8b8b8", "000000");
						content = content.replaceAll("1a1a1a", "888888");
						content = content.replaceAll("b8b7b7", "ffffff");
						content = content.replaceAll("<body bgcolor=\"#2a2a2a\">", "");
						content = content.replaceAll("</body>", "");
						content = content.replaceAll("<font",
								"<font style=\"word-break:break-all;word-wrap:break-word;\"");
						content = content.replaceAll("class=\"img-responsive\"",
								"style=\"display:block;height:auto;max-width:100%;\"");
						ShareToEvernote.getInstance(mContext).shareNote(mContext, mMainTitle, mMainUrl,
								content, mNoteCreateCallback);

					}
				}).start();
			} else {
				content = "";
				ShareToEvernote.getInstance(this).shareNote(this, mMainTitle, mMainUrl, content,
						mNoteCreateCallback);
			}

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
			mCustomWebView.loadUrl("javascript:(function(){"
					+ "var objs = document.getElementsByTagName(\"img\"); "
					+ "for(var i=0;i<objs.length;i++)  " + "{"
					+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
					+ "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
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

	public class DownloadWebImgTask extends AsyncTask<String, String, Void> {
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			if (mCustomWebView != null && !mPaused)
				mCustomWebView.loadUrl("javascript:(function(){"
						+ "var objs = document.getElementsByTagName(\"img\"); "
						+ "for(var i=0;i<objs.length;i++)  " + "{"
						+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
						+ "    var imgOriSrc = objs[i].getAttribute(\"ori_link\"); " + " if(imgOriSrc == \""
						+ values[0] + "\"){ " + "    objs[i].setAttribute(\"src\",imgSrc);}" + "}" + "})()");
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mCustomWebView != null && !mPaused)
				mCustomWebView.loadUrl("javascript:(function(){"
						+ "var objs = document.getElementsByTagName(\"img\"); "
						+ "for(var i=0;i<objs.length;i++)  " + "{"
						+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
						+ "    objs[i].setAttribute(\"src\",imgSrc);" + "}" + "})()");
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
					urlCon = (HttpURLConnection) url.openConnection();
					urlCon.setRequestMethod("GET");
					urlCon.setRequestProperty("Accept-Encoding", "identity");
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
