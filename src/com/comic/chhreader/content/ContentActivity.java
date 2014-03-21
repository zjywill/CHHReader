package com.comic.chhreader.content;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.comic.chhreader.Constants;
import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.detail.DetailActivity;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.Utils;
import com.comic.chhreader.view.NetworkDialog;
import com.comic.chhreader.view.PullDownRefreashListView;
/*import com.comic.seexian.detail.DetailActivity;
 import com.comic.seexian.utils.SeeXianNetUtils;
 import com.comic.seexian.utils.SeeXianUtils;*/

public class ContentActivity extends Activity implements OnItemClickListener {

	private static final int MSG_GET_LOCAL_DATA_FINISH = 1;
	private static final int MSG_GET_NET_DATA_FINISH = 2;

	private Context mCtx;

	private PullDownRefreashListView mListView;
	private ContentAdapter mListAdapter;

	private View mLoadingView;
	private TextView mLoadingText;
	private ProgressBar mLoadingProgress;
	private View mLoadMoreView;
	private Button mLoadMoreBtn;

	private ContentResolver mContentResolver;

	private NetworkDialog mNetworkDialog = null;

	private String mMainTitle;
	private String mCategory;

	// ------------------------------------------------------------------

	private View mRefreshViewInside;
	private View mRefreshHorizontalImage;
	private View mRefreshHorizontalProgress;

	// ------------------------------------------------------------------

	private String mLatestId;

	private int mSelectedItem;

	private Handler mMessageHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_GET_LOCAL_DATA_FINISH : {
					mLoadingView.setVisibility(View.GONE);
				}
					break;
				case Constants.MESSAGE_NETWORK_ERROR : {
					if (mNetworkDialog == null) {
						mNetworkDialog = new NetworkDialog(mCtx, R.style.Theme_dialog);
					}
					mNetworkDialog.show();
				}
					break;
				default :
					break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCtx = this;

		setContentView(R.layout.content_layout);

		mListView = (PullDownRefreashListView) findViewById(R.id.user_history_list);

		mLoadingView = (View) findViewById(R.id.user_empty_view);
		mLoadingText = (TextView) findViewById(R.id.user_empty_text);
		mLoadingProgress = (ProgressBar) findViewById(R.id.user_loading_progress);

		mLoadingText.setText(R.string.loading);

		mRefreshViewInside = (View) findViewById(R.id.pull_to_refresh_a);
		mRefreshHorizontalImage = (View) findViewById(R.id.refreash_header_image_a);
		mRefreshHorizontalProgress = (View) findViewById(R.id.refreash_header_progress_a);

		mListView.setDivider(null);
		mListAdapter = new ContentAdapter(this, null);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		mListView.addCustomView(mRefreshViewInside, mRefreshHorizontalImage, mRefreshHorizontalProgress);
		mListView.setOnCreateContextMenuListener(this);
		// mListView.setOnScrollListener(mScrollListener);//no need this

		mLoadMoreView = getLayoutInflater().inflate(R.layout.loadmore_view, null);
		mLoadMoreBtn = (Button) mLoadMoreView.findViewById(R.id.loadmore_btn);
		mLoadMoreBtn.setOnClickListener(mLoadMoreClicked);
		mListView.addFooterView(mLoadMoreView);

		Intent infointent = getIntent();
		mMainTitle = infointent.getStringExtra("title");
		mCategory = infointent.getStringExtra("category");

		intActionBar();

		mListView.setOnRefreshListener(new PullDownRefreashListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new NetDataFetch().execute("1213");
			}
		});

		if (!Utils.isNetworkAvailable(getApplicationContext())) {
			Loge.i("Net unavilable not show loadmore");
			mLoadMoreView.setVisibility(View.GONE);
		}

		mListView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);

		new LocalDataFetch().execute();
	}
	void intActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(mMainTitle);
			actionBar.setIcon(R.drawable.chh_icon);
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(ContentActivity.this, DetailActivity.class);
		Cursor cur = (Cursor) mListAdapter.getItem(position);
		if (cur != null) {
			intent.putExtra("title", cur.getString(1));
			intent.putExtra("url", cur.getString(5));
			startActivity(intent);
		}
	}

	AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if ((firstVisibleItem + visibleItemCount) >= totalItemCount) {
				Loge.i("Scroll to the end");
			}
		}
	};

	View.OnClickListener mLoadMoreClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Loge.i("Load more clicked");
			new NetDataFetch().execute("13241");// TODO
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class LocalDataFetch extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			// get data from local data base
			if (mContentResolver == null) {
				mContentResolver = getContentResolver();
			}
			String[] projection = new String[6];
			projection[0] = DataProvider.KEY_MAIN_ID;
			projection[1] = DataProvider.KEY_MAIN_TITLE;
			projection[2] = DataProvider.KEY_MAIN_PIC_URL;
			projection[3] = DataProvider.KEY_MAIN_SHORTCUT;
			projection[4] = DataProvider.KEY_MAIN_PUBLISH_DATE;
			projection[5] = DataProvider.KEY_MAIN_URL;

			String selection = DataProvider.KEY_MAIN_TYPE + "='" + "content" + "'" + " AND " + DataProvider.KEY_MAIN_CATEGORY + "='" + mCategory + "'";
			Loge.i("selection = " + selection);

			Cursor cursor = mContentResolver.query(DataProvider.CONTENT_URI_MAIN_DATA, projection, selection, null, DataProvider.KEY_MAIN_PUBLISH_DATE + " DESC");
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			if (cursor != null) {
				Loge.d("cursor count = " + cursor.getCount());
				if (cursor.getCount() == 0) {
					new NetDataFetch().execute("13241");
				} else {
					mListAdapter.swapCursor(cursor);
					mListView.setVisibility(View.VISIBLE);
					mLoadingText.setText(R.string.loading);
					mLoadingView.setVisibility(View.GONE);

					mLoadMoreView.setVisibility(View.VISIBLE);
					mLoadMoreBtn.setClickable(true);
					mLoadMoreBtn.setText(R.string.load_more);
				}
			}
			super.onPostExecute(cursor);
		}

	};

	class NetDataFetch extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!Utils.isNetworkAvailable(getBaseContext())) {
				if (mNetworkDialog == null) {
					mNetworkDialog = new NetworkDialog(mCtx, R.style.Theme_dialog);
					mNetworkDialog.show();
				} else {
					Toast.makeText(mCtx, R.string.no_network, Toast.LENGTH_SHORT).show();
				}
			}
			if (mListAdapter.getCount() == 0) {
				mLoadMoreView.setVisibility(View.GONE);
			} else {
				mLoadMoreBtn.setClickable(false);
				mLoadMoreBtn.setText(R.string.loading);
			}
		}

		@Override
		protected String doInBackground(String... params) {
			if (Utils.isNetworkAvailable(getBaseContext())) {
				String loadingUrl = null;
				if (params != null && params.length > 0) {
					loadingUrl = params[0];
				}
				if (loadingUrl == null) {
					return "fail";
				}
				Loge.i("loading URL = " + loadingUrl);

				ArrayList<ContentData> tempListData = new ArrayList<ContentData>();

				for (int i = 0; i < 30; i++) {
					ContentData item = new ContentData();
					item.mContentTitle = "venue8pro简单评测——从metro应用看windows平板";
					item.mContentPic = "http://www.chiphell.com/data/attachment/portal/201403/11/194032o30h6opcpy6defep.jpg";
					item.mContentURL = "http://www.chiphell.com/thread-986442-1-1.html";
					item.mContentShortcut = "venue8pro简单评测 ——从metro应用看windows平板 正题之前，首先说一下自己的移动电子产品吧：一台nexus4手机，一台mx3手机，一台X220笔记本，一个LG的蓝牙耳机。以前买过一个昂达的7寸平板，一 ...";
					item.mContentPostDate = i * 1000;
					item.mContentType = "content";
					tempListData.add(item);
				}

				if (mContentResolver == null) {
					mContentResolver = getContentResolver();
				}
				// save data and update data
				if (tempListData.size() > 0) {
					ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();

					for (ContentData item : tempListData) {
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_MAIN_DATA).withValue(DataProvider.KEY_MAIN_TITLE, item.mContentTitle).withValue(DataProvider.KEY_MAIN_PIC_URL, item.mContentPic).withValue(DataProvider.KEY_MAIN_CATEGORY, mCategory).withValue(DataProvider.KEY_MAIN_SHORTCUT, item.mContentShortcut).withValue(DataProvider.KEY_MAIN_URL, item.mContentURL).withValue(DataProvider.KEY_MAIN_PUBLISH_DATE, item.mContentPostDate).withValue(DataProvider.KEY_MAIN_TYPE, item.mContentType);
						opertions.add(builder.build());
					}

					try {
						mContentResolver.applyBatch(DataProvider.DB_AUTHOR, opertions);
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (OperationApplicationException e) {
						e.printStackTrace();
					}

				}
				return "success";
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("success")) {
				new LocalDataFetch().execute();
			} else {
				mLoadMoreView.setVisibility(View.VISIBLE);
				mLoadMoreBtn.setClickable(true);
				mLoadMoreBtn.setText(R.string.load_more);
				if (mListAdapter.getCount() == 0) {
					mLoadingProgress.setVisibility(View.GONE);
					mLoadingText.setText(R.string.empty_text);
					mListView.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					mLoadMoreView.setVisibility(View.GONE);
				}
			}
			mListView.onRefreshComplete();
		}
	};
}
