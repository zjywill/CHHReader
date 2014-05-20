package com.comic.chhreader;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.comic.chhreader.clean.CleanService;
import com.comic.chhreader.content.ContentActivity;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.SubItemData;
import com.comic.chhreader.data.TopicData;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.SharedPreferencesUtils;
import com.comic.chhreader.utils.Utils;
import com.comic.chhreader.view.NetworkDialog;

public class MainActivity extends Activity implements OnItemClickListener, LoaderCallbacks<Cursor> {

	private static final int LOADER_ID_LOACL = 103;

	private static final long UPGRADE_GAP = DateUtils.DAY_IN_MILLIS;

	private GridView mGrid;
	private MainGridAdapter mGirdAdapter;

	private ProgressBar mLoadingProgress;

	private Context mContext = this;
	private NetworkDialog mNetworkDialog = null;

	boolean updating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent cleanService = new Intent();
		cleanService.setClass(MainActivity.this, CleanService.class);
		startService(cleanService);

		mGrid = (GridView) findViewById(R.id.main_gird);
		mGrid.setOnItemClickListener(this);
		mGirdAdapter = new MainGridAdapter(this, null);
		mGrid.setAdapter(mGirdAdapter);

		initActionBar();

		mGrid.setAlpha(0f);
		mGrid.animate().alpha(1f).setDuration(800).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mGrid.setVisibility(View.VISIBLE);
			}
		});

		getLoaderManager().initLoader(LOADER_ID_LOACL, null, this);

		if (Utils.isWifiAvailable(mContext) && !updating) {
			new FetchDataTaskNet().execute();
		}
	}

	void initActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
			actionBar.setIcon(R.drawable.title_icon);

			Loge.i("action bar setCustomView");
			LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View actionbarView = inflator.inflate(R.layout.main_activity_actionbar, null);
			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
					Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			actionBar.setCustomView(actionbarView, lp);
			actionBar.setDisplayShowCustomEnabled(true);

			mLoadingProgress = (ProgressBar) actionbarView.findViewById(R.id.action_loading);
			mLoadingProgress.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_do_refresh: {
				Loge.i("Options Selected = do_refresh");
				if (!updating)
					new FetchDataTaskNet().execute();
			}
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(MainActivity.this, ContentActivity.class);
		Cursor cur = (Cursor) mGirdAdapter.getItem(position);
		if (cur != null) {
			intent.putExtra("title", cur.getString(1));
			intent.putExtra("category", cur.getInt(3));
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(LOADER_ID_LOACL, null, MainActivity.this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		Loge.i("onCreateLoader");
		switch (loaderID) {
			case LOADER_ID_LOACL: {
				String[] projection = new String[4];
				projection[0] = DataProvider.KEY_TOPIC_ID;
				projection[1] = DataProvider.KEY_TOPIC_NAME;
				projection[2] = DataProvider.KEY_TOPIC_IMAGE_URL;
				projection[3] = DataProvider.KEY_TOPIC_PK;

				return new CursorLoader(this, DataProvider.CONTENT_URI_TOPIC_DATA, null, null, null,
						DataProvider.KEY_TOPIC_PK);
			}
			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
		Loge.i("onLoadFinished id: " + loader.getId());
		switch (loader.getId()) {
			case LOADER_ID_LOACL: {
				if (cur != null && cur.getCount() > 0) {
					Loge.i("get data from local count = " + cur.getCount());
					mGirdAdapter.swapCursor(cur);
					mGirdAdapter.notifyDataSetChanged();
				} else {
					Loge.i("Cursor is null or count == 0");
					if (!updating)
						new FetchDataTaskNet().execute();
				}
			}
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	class FetchDataTaskNet extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			updating = true;
			mLoadingProgress.setVisibility(View.VISIBLE);
			if (!Utils.isNetworkAvailable(getBaseContext())) {
				if (mNetworkDialog == null) {
					mNetworkDialog = new NetworkDialog(mContext, R.style.Theme_dialog);
					mNetworkDialog.show();
				} else {
					Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (Utils.isNetworkAvailable(getBaseContext())) {

				ArrayList<TopicData> topicsData = null;
				ArrayList<SubItemData> subItemDatas = null;

				boolean fetchNetData = true;

				if (DataBaseUtils.isTopicDataExist(mContext)) {
					fetchNetData = false;
					long timeNow = System.currentTimeMillis();
					if ((timeNow - SharedPreferencesUtils.getUpdateTime(mContext)) > UPGRADE_GAP) {
						fetchNetData = true;
					}
				}

				if (fetchNetData) {
					topicsData = CHHNetUtils.getTopicsDate(mContext);
					if (topicsData == null) {
						return "fail";
					}

					subItemDatas = CHHNetUtils.getAllSubItemsDate(mContext);
					if (subItemDatas == null) {
						return "fail";
					}
					DataBaseUtils.deleteAllSubItemData(mContext);
					DataBaseUtils.saveSubItemData(mContext, subItemDatas);

					SharedPreferencesUtils.saveUpdateTime(mContext, System.currentTimeMillis());
				} else {
					topicsData = DataBaseUtils.getTopicData(mContext);
					subItemDatas = DataBaseUtils.getSubItemData(mContext);
				}

				if (topicsData == null) {
					return "fail";
				}

				if (subItemDatas == null) {
					return "fail";
				}

				ArrayList<ContentData> contentDatas = CHHNetUtils.getMainContentItemsDate(mContext);
				if (contentDatas == null) {
					return "fail";
				}

				for (int i = contentDatas.size() - 1; i >= 0; i--) {
					ContentData contentItem = contentDatas.get(i);
					for (SubItemData subItemData : subItemDatas) {
						if (contentItem.mSubItemType == subItemData.mPk) {
							contentItem.mTopicType = subItemData.mTopic;
							for (TopicData tData : topicsData) {
								if (tData.mPk == subItemData.mTopic && contentItem.mValid) {
									tData.mImageUrl = contentItem.mImageUrl;
									break;
								}
							}
						}
					}
				}
				DataBaseUtils.saveContentItemData(mContext, contentDatas);

				if (topicsData != null && topicsData.size() > 0) {
					DataBaseUtils.deleteAllTopicData(mContext);
				}
				DataBaseUtils.saveTopicData(mContext, topicsData);

				topicsData.clear();
				subItemDatas.clear();
				contentDatas.clear();
				return "success";
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mLoadingProgress.setVisibility(View.GONE);
			if (result.equals("success")) {
				getLoaderManager().restartLoader(LOADER_ID_LOACL, null, MainActivity.this);
			} else {
				Toast.makeText(mContext, R.string.no_update, Toast.LENGTH_SHORT).show();
			}
			updating = false;
		}

	}
}
