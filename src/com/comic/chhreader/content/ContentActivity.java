package com.comic.chhreader.content;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.SubItemData;
import com.comic.chhreader.detail.DetailActivity;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.Utils;
import com.comic.chhreader.view.NetworkDialog;
import com.comic.chhreader.view.PullDownRefreashListView;

public class ContentActivity extends Activity implements OnItemClickListener {
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
	private int mCategory;

	// ------------------------------------------------------------------

	private View mRefreshViewInside;
	private View mRefreshHorizontalImage;
	private View mRefreshHorizontalProgress;

	// ------------------------------------------------------------------

	private int mLatestId;

	private ContentData mFirstItem;
	private ContentData mLastItem;

	private boolean updating = false;
	private boolean first = true;

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
		mListAdapter = new ContentAdapter(this);
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
		mCategory = infointent.getIntExtra("category", -1);

		intActionBar();

		mListView.setOnRefreshListener(new PullDownRefreashListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (!updating)
					new NetDataFetch().execute("update");
			}
		});

		if (!Utils.isNetworkAvailable(getApplicationContext())) {
			Loge.i("Net unavilable not show loadmore");
			mLoadMoreView.setVisibility(View.GONE);
		}

		mListView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);

		new LocalDataFetch().execute();

		if (Utils.isWifiAvailable(mCtx) && !updating) {
			new NetDataFetch().execute("update");
		}
	}

	void intActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(mMainTitle);
			actionBar.setIcon(R.drawable.chh_icon);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
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
			if (!updating)
				new NetDataFetch().execute("more");
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
		Cursor cursor = mListAdapter.getCursor();
		if (cursor != null) {
			cursor.close();
		}
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
			projection[3] = DataProvider.KEY_MAIN_CONTENT;
			projection[4] = DataProvider.KEY_MAIN_PUBLISH_DATE;
			projection[5] = DataProvider.KEY_MAIN_URL;

			String selection = DataProvider.KEY_MAIN_TOPIC_PK + "='" + mCategory + "'"//
					+ " AND " + DataProvider.KEY_MAIN_VALID + "='" + "1" + "'";
			Loge.i("selection = " + selection);

			Cursor cursor = mContentResolver.query(DataProvider.CONTENT_URI_MAIN_DATA, projection, selection, null, DataProvider.KEY_MAIN_PUBLISH_DATE + " DESC");

			if (cursor != null) {
				mLatestId = cursor.getCount();
				if (cursor.moveToFirst()) {
					if (mFirstItem == null) {
						mFirstItem = new ContentData();
					}
					mFirstItem.mLink = cursor.getString(5);
				}
				if (cursor.moveToLast()) {
					if (mLastItem == null) {
						mLastItem = new ContentData();
					}
					mLastItem.mLink = cursor.getString(5);
				}
			}
			return cursor;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			if (cursor != null) {
				Loge.d("cursor count = " + cursor.getCount());
				if (cursor.getCount() == 0) {
					if (!updating)
						new NetDataFetch().execute("update");
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
			Loge.d("NetDataFetch begin fetch data");
			updating = true;
			if (!Utils.isNetworkAvailable(getBaseContext())) {
				if (mNetworkDialog == null) {
					mNetworkDialog = new NetworkDialog(mCtx, R.style.Theme_dialog);
					mNetworkDialog.show();
				} else {
					Toast.makeText(mCtx, R.string.no_network, Toast.LENGTH_SHORT).show();
				}
			}
			if (first) {
				first = false;
			} else {
				if (mListAdapter.getCount() == 0) {
					mLoadMoreView.setVisibility(View.GONE);
				}
			}

			mLoadMoreBtn.setClickable(false);
			mLoadMoreBtn.setText(R.string.loading);
		}

		@Override
		protected String doInBackground(String... params) {
			if (Utils.isNetworkAvailable(getBaseContext())) {
				String loadingWay = null;
				if (params != null && params.length > 0) {
					loadingWay = params[0];
				}
				if (loadingWay == null) {
					return "fail";
				}
				Loge.i("loadingWay = " + loadingWay);

				if (mContentResolver == null) {
					mContentResolver = mCtx.getContentResolver();
				}

				String[] subItemProjection = new String[1];
				subItemProjection[0] = DataProvider.KEY_SUBITEM_PK;

				String selection = DataProvider.KEY_SUBITEM_TOPIC_PK + "='" + mCategory + "'";

				Cursor subItemCursor = mContentResolver.query(DataProvider.CONTENT_URI_SUBITEM_DATA, subItemProjection, selection, null, null);

				ArrayList<SubItemData> subItemDatas = null;
				if (subItemCursor != null) {
					if (subItemCursor.getCount() > 0 && subItemCursor.moveToFirst()) {
						do {
							if (subItemDatas == null) {
								subItemDatas = new ArrayList<SubItemData>();
							}
							SubItemData itemData = new SubItemData();
							itemData.mPk = subItemCursor.getInt(subItemCursor.getColumnIndex(DataProvider.KEY_SUBITEM_PK));
							Loge.d("SubItemData pk: " + itemData.mPk);
							subItemDatas.add(itemData);
						} while (subItemCursor.moveToNext());
					}
				}
				if (subItemDatas == null) {
					return "fail";
				}

				ArrayList<ContentData> tempListData = new ArrayList<ContentData>();

				for (SubItemData itemData : subItemDatas) {
					int page = 1;
					if (loadingWay.equals("more")) {
						boolean hasInvalid = false;
						Loge.d("mLatestId: " + mLatestId);
						if ((mLatestId % 10) > 5) {
							hasInvalid = true;
						}
						page = mLatestId / 10 + 1;
						if (hasInvalid) {
							page++;
						}

						if (mLatestId <= 5) {
							page = 1;
						}
					}
					Loge.d("load more page: " + page);
					ArrayList<ContentData> contentDatasTemp = CHHNetUtils.getContentItemsDate(mCtx, mCategory, itemData.mPk, page);
					if (contentDatasTemp != null) {
						tempListData.addAll(contentDatasTemp);
					}
				}

				boolean updated = true;
				boolean no_update = false;
				if (tempListData.size() > 0) {
					if (loadingWay.equals("update")) {
						long postdata = 0;
						int id = 0;
						boolean anyInvaild = false;
						for (int i = 0; i < tempListData.size(); i++) {
							ContentData itemContent = tempListData.get(i);
							if (!itemContent.mValid) {
								anyInvaild = true;
							}
							if (itemContent.mValid && itemContent.mPostDate > postdata) {
								postdata = itemContent.mPostDate;
								id = i;
							}
						}
						ContentData first = tempListData.get(id);
						Loge.d("load more first 1: " + mFirstItem.mLink);
						Loge.d("load more first 2: " + first.mLink);
						if (!first.mLink.equals(mFirstItem.mLink)) {
							DataBaseUtils.updateTopicImage(mCtx, mCategory, first.mImageUrl);
						} else {
							no_update = true;
						}
						if (anyInvaild) {
							Loge.d("anyInvaild: " + anyInvaild);
							updated = true;
						}
					} else {
						long postdata = tempListData.get(0).mPostDate;
						int id = 0;
						for (int i = 0; i < tempListData.size(); i++) {
							ContentData itemContent = tempListData.get(i);
							if (itemContent.mPostDate < postdata) {
								postdata = itemContent.mPostDate;
								id = i;
							}
						}
						ContentData last = tempListData.get(id);
						Loge.d("load more last 1: " + mLastItem.mLink);
						Loge.d("load more last 2: " + last.mLink);
						if (last.mLink.equals(mLastItem.mLink)) {
							updated = false;
						}
					}
				} else {
					updated = false;
				}

				if (updated) {
					DataBaseUtils.saveContentItemData(mCtx, tempListData);
				}

				subItemDatas.clear();
				tempListData.clear();

				if (no_update && 10 <= mLatestId) {
					return loadingWay;
				} else if (updated) {
					return "success";
				} else {
					return loadingWay;
				}
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Loge.d("NetDataFetch result: " + result);
			if (result.equals("success")) {
				new LocalDataFetch().execute();
			} else {
				if (result.equals("update")) {
					Toast.makeText(mCtx, R.string.no_update, Toast.LENGTH_SHORT).show();
				} else if (result.equals("more")) {
					Toast.makeText(mCtx, R.string.no_more_data, Toast.LENGTH_SHORT).show();
				}
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
			updating = false;
		}
	};
}
