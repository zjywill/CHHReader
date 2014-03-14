package com.comic.chhreader.content;

import java.util.ArrayList;

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
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.comic.chhreader.Constants;
import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.MainGridData;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.view.NetworkDialog;
import com.comic.chhreader.view.PullDownRefreashListView;
/*import com.comic.seexian.detail.DetailActivity;
 import com.comic.seexian.utils.SeeXianNetUtils;
 import com.comic.seexian.utils.SeeXianUtils;*/

public class ContentActivity extends Activity
		implements
			OnItemClickListener,
			OnItemLongClickListener,
			OnCreateContextMenuListener {

	private Context mCtx;

	private PullDownRefreashListView mListView;
	private ContentAdapter mListAdapter;

	private View mLoadingView;
	private ProgressBar mLoadingProgress;
	private TextView mLoadingText;

	private ContentResolver mContentResolver;

	private ArrayList<ContentData> mListData = new ArrayList<ContentData>();

	private NetworkDialog mNetworkDialog = null;

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
		mLoadingProgress = (ProgressBar) findViewById(R.id.user_loading_progress);
		mLoadingText = (TextView) findViewById(R.id.user_empty_text);

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
		mListView.setOnItemLongClickListener(this);

		Intent infointent = getIntent();
		mCategory = infointent.getStringExtra("category");

		mListView.setOnRefreshListener(new PullDownRefreashListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				// new GetDataFromNetTask().execute(null);
			}
		});

		new LocalDataFetch().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		mSelectedItem = arg2;
		return false;
	}

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
			String[] projection = new String[3];
			projection[0] = DataProvider.KEY_MAIN_TITLE;
			projection[1] = DataProvider.KEY_MAIN_PIC_URL;
			projection[2] = DataProvider.KEY_MAIN_SHORTCUT;

			String selection = DataProvider.KEY_MAIN_CATEGORY + "='" + "main" + "'";
			Loge.i("selection = " + selection);

			Cursor cursor = mContentResolver.query(DataProvider.CONTENT_URI_MAIN_DATA, projection, selection, null, DataProvider.KEY_MAIN_PUBLISH_DATE + " DESC");
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			if (cursor != null) {
				Loge.d("cursor count = " + cursor.getCount());
				if (cursor.getCount() == 0) {
					new NetDataFetch().execute();
				} else {
					mListAdapter.swapCursor(cursor);
				}
			}
			super.onPostExecute(cursor);
		}

	};

	class NetDataFetch extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {

			ArrayList<ContentData> tempListData = new ArrayList<ContentData>();

			for (int i = 0; i < 30; i++) {
				ContentData item = new ContentData();
				item.mContentTitle = "venue8pro简单评测——从metro应用看windows平板";
				item.mContentPic = "http://www.chiphell.com/data/attachment/portal/201403/11/194032o30h6opcpy6defep.jpg";
				item.mContentURL = "http://www.chiphell.com/thread-986442-1-1.html";
				item.mContentShortcut = "venue8pro简单评测 ——从metro应用看windows平板 正题之前，首先说一下自己的移动电子产品吧：一台nexus4手机，一台mx3手机，一台X220笔记本，一个LG的蓝牙耳机。以前买过一个昂达的7寸平板，一 ...";
				item.mContentPostDate = i * 1000;
			}

			if (mContentResolver == null) {
				mContentResolver = getContentResolver();
			}
			// save data and update data
			if (tempListData.size() > 0) {
				ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();

				for (ContentData item : tempListData) {
					ContentProviderOperation.Builder builder = 
							ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_MAIN_DATA).
							withValue(DataProvider.KEY_MAIN_TITLE, item.mContentTitle).
							withValue(DataProvider.KEY_MAIN_PIC_URL, item.mContentPic).
							withValue(DataProvider.KEY_MAIN_CATEGORY, mCategory).
							withValue(DataProvider.KEY_MAIN_SHORTCUT, item.mContentShortcut).
							withValue(DataProvider.KEY_MAIN_URL, item.mContentURL).
							withValue(DataProvider.KEY_MAIN_PUBLISH_DATE, item.mContentPostDate);
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

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	};
}
