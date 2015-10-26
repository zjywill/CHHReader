package com.comic.chhreader;

import java.util.ArrayList;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.SubItemData;
import com.comic.chhreader.data.TopicData;
import com.comic.chhreader.detail.DetailActivity;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.Utils;

public class ContentListFragment extends SwipeRefreshListFragment implements LoaderCallbacks<Cursor>,
		OnItemClickListener {

	public static final int LOADER_ID_LOACL = 105;

	private ContentAdapter mListAdapter;

	private int mCategory = 0;
	private int mDataSize = 0;

	private ContentResolver mContentResolver;

	private long mImageTimeStamp;
	private int mLatestId;
	private ContentData mFirstItem = new ContentData();
	private ContentData mLastItem = new ContentData();

	private boolean updating = true;
	private boolean nomore = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mListAdapter = new ContentAdapter(getActivity());
		setListAdapter(mListAdapter);
		getListView().setDivider(null);
		getListView().setOnItemClickListener(this);
		getListView().setOnScrollListener(mScrollListener);

		setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {

				initiateRefresh();
			}
		});
		setColorScheme(R.color.red_300, R.color.red_400, R.color.red_500, R.color.red_600);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(getActivity(), DetailActivity.class);
		Cursor cur = (Cursor) mListAdapter.getItem(position);
		if (cur != null) {
			intent.putExtra("title", cur.getString(1));
			intent.putExtra("url", cur.getString(5));
			startActivity(intent);
		}
	}

	private void initiateRefresh() {
		new GetContentDataTask().execute("update");
	}

	AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if ((firstVisibleItem + visibleItemCount) >= totalItemCount) {
				if (Utils.isWifiAvailable(getActivity()) && !updating && !nomore && mCategory == 0) {
					Loge.d("Scroll to the end get more data");
					new GetContentDataTask().execute("more");
				}
			}
		}
	};

	public void initData(int category) {
		mCategory = category;
		getLoaderManager().initLoader(LOADER_ID_LOACL, null, ContentListFragment.this);
	}

	public void reloadData(int category) {
		mCategory = category;
		if (getActivity() != null) {
			android.support.v4.app.LoaderManager lm = getLoaderManager();
			if (lm != null) {
				lm.restartLoader(LOADER_ID_LOACL, null, ContentListFragment.this);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_ID_LOACL: {
				String[] projection = new String[6];
				projection[0] = DataProvider.KEY_MAIN_ID;
				projection[1] = DataProvider.KEY_MAIN_TITLE;
				projection[2] = DataProvider.KEY_MAIN_PIC_URL;
				projection[3] = DataProvider.KEY_MAIN_CONTENT;
				projection[4] = DataProvider.KEY_MAIN_PUBLISH_DATE;
				projection[5] = DataProvider.KEY_MAIN_URL;

				String selection = null;
				if (mCategory != 0) {
					selection = DataProvider.KEY_MAIN_TOPIC_PK + "='" + mCategory + "'"//
							+ " AND " + DataProvider.KEY_MAIN_VALID + "='" + "1" + "'";
				} else {
					selection = DataProvider.KEY_MAIN_VALID + "='" + "1" + "'";
				}

				Loge.i("selection = " + selection);

				return new CursorLoader(getActivity(), DataProvider.CONTENT_URI_MAIN_DATA, projection,
						selection, null, DataProvider.KEY_MAIN_PUBLISH_DATE + " DESC");
			}
			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
		switch (loader.getId()) {
			case LOADER_ID_LOACL: {
				if (cur != null && cur.getCount() >= 0) {
					Loge.i("get data from local count = " + cur.getCount());
					mDataSize = cur.getCount();
					mListAdapter.swapCursor(cur);
					mListAdapter.notifyDataSetChanged();
					setRefreshing(false);

					mLatestId = cur.getCount();
					if (cur.moveToFirst()) {
						if (mFirstItem == null) {
							mFirstItem = new ContentData();
						}
						mFirstItem.mLink = cur.getString(5);
					}
					if (cur.moveToLast()) {
						if (mLastItem == null) {
							mLastItem = new ContentData();
						}
						mLastItem.mLink = cur.getString(5);
					}
					updating = false;
				} else {
					Loge.i("Cursor is null or count == 0");

				}
			}
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	};

	private void onRefreshComplete(String result) {
		getLoaderManager().restartLoader(LOADER_ID_LOACL, null, this);
		updating = false;
		setRefreshing(false);
		Loge.i("onRefreshComplete result = " + result);
		if (result != null && result.equals("more")) {
			Toast.makeText(getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT).show();
			nomore = true;
		}
	}

	private class GetContentDataTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!Utils.isNetworkAvailable(ContentListFragment.this.getActivity())) {
				Toast.makeText(ContentListFragment.this.getActivity(), R.string.no_network,
						Toast.LENGTH_SHORT).show();
			}
			updating = true;
		}

		@Override
		protected String doInBackground(String... params) {
			return getCategoryData(params);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			onRefreshComplete(result);
		}

		private String getCategoryData(String... params) {
			if (Utils.isNetworkAvailable(getActivity())) {
				String loadingWay = null;
				if (params != null && params.length > 0) {
					loadingWay = params[0];
				}
				if (loadingWay == null) {
					return "fail";
				}

				Loge.d("getCategoryData loadingWay: " + loadingWay);

				if (mContentResolver == null) {
					mContentResolver = getActivity().getContentResolver();
				}

				String[] subItemProjection = new String[2];
				subItemProjection[0] = DataProvider.KEY_SUBITEM_PK;
				subItemProjection[1] = DataProvider.KEY_SUBITEM_TOPIC_PK;

				String selection = null;
				if (mCategory != 0) {
					selection = DataProvider.KEY_SUBITEM_TOPIC_PK + "='" + mCategory + "'";
				}

				Cursor subItemCursor = mContentResolver.query(DataProvider.CONTENT_URI_SUBITEM_DATA,
						subItemProjection, selection, null, null);

				ArrayList<SubItemData> subItemDatas = null;
				if (subItemCursor != null) {
					if (subItemCursor.getCount() > 0 && subItemCursor.moveToFirst()) {
						do {
							if (subItemDatas == null) {
								subItemDatas = new ArrayList<SubItemData>();
							}
							SubItemData itemData = new SubItemData();
							itemData.mPk = subItemCursor.getInt(subItemCursor
									.getColumnIndex(DataProvider.KEY_SUBITEM_PK));
							itemData.mTopic = subItemCursor.getInt(subItemCursor
									.getColumnIndex(DataProvider.KEY_SUBITEM_TOPIC_PK));
							subItemDatas.add(itemData);
						} while (subItemCursor.moveToNext());
					}
					subItemCursor.close();
				}
				if (subItemDatas == null) {
					return "fail";
				}

				ArrayList<ContentData> tempListData = new ArrayList<ContentData>();

				int page = 1;

				ArrayList<ContentData> contentDatasTemp = null;

				if (loadingWay.equals("more")) {
					boolean hasInvalid = false;
					Loge.d("mLatestId: " + mLatestId);
					if ((mLatestId % 20) > 5) {
						hasInvalid = true;
					}
					page = mLatestId / 20 + 1;
					if (hasInvalid) {
						page++;
					}

					if (mLatestId <= 5) {
						page = 1;
					}
				}
				if (mCategory == 0) {
					contentDatasTemp = CHHNetUtils.getContentItemsDate(getActivity(), page);
					if (contentDatasTemp != null) {
						for (int i = contentDatasTemp.size() - 1; i >= 0; i--) {
							ContentData contentItem = contentDatasTemp.get(i);
							for (SubItemData subItemData : subItemDatas) {
								if (contentItem.mSubItemType == subItemData.mPk) {
									contentItem.mTopicType = subItemData.mTopic;
								}
							}
						}
					}
				}
				if (contentDatasTemp == null) {
					return "fail";
				}
				tempListData.addAll(contentDatasTemp);

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
						if (!first.mLink.equals(mFirstItem.mLink) && first.mPostDate > mImageTimeStamp) {
							DataBaseUtils.updateTopicImage(getActivity(), mCategory, first.mImageUrl);
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
							if (page != 1) {
								updated = false;
							}
						}
					}
				} else {
					updated = false;
				}

				if (updated) {
					DataBaseUtils.saveContentItemData(getActivity(), tempListData);
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
	}
}
