package com.comic.chhreader;

import java.util.ArrayList;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.SubItemData;
import com.comic.chhreader.data.TopicData;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.DataBaseUtils;
import com.comic.chhreader.utils.SharedPreferencesUtils;
import com.comic.chhreader.utils.Utils;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	private static final int LOADER_ID_LOACL = 103;
	private static final long UPGRADE_GAP = DateUtils.DAY_IN_MILLIS;

	private Context mContext;

	private ContentListFragment mMainDataFrgment;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CategoryAdapter mCategoryAdapter;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	private boolean updating = false;

	private int mCategory = 0;

	private boolean mFirstLoad = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);

		initDrawer();

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		mMainDataFrgment = new ContentListFragment();
		transaction.replace(R.id.content_frame, mMainDataFrgment);
		transaction.commit();
	}

	private void initDrawer() {
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mCategoryAdapter = new CategoryAdapter(this, null);
		mDrawerList.setAdapter(mCategoryAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public void onAttachedToWindow() {
		if (mFirstLoad) {
			loadData();
			mFirstLoad = false;
		}
	}

	private void loadData() {
		getLoaderManager().initLoader(LOADER_ID_LOACL, null, this);
		if (Utils.isWifiAvailable(mContext) && !updating) {
			new FetchDataTaskNet().execute();
		} else {
			if (mMainDataFrgment != null) {
				mMainDataFrgment.reloadData(mCategory);
			}
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
			mDrawerLayout.closeDrawers();
			Cursor cur = (Cursor) mCategoryAdapter.getItem(position);
			mCategory = cur.getInt(cur.getColumnIndex(DataProvider.KEY_TOPIC_PK));
			Loge.d("onItemClick mCategory: " + mCategory);
			if (mMainDataFrgment != null) {
				mMainDataFrgment.reloadData(mCategory);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void selectItem(final int position) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Cursor cur = (Cursor) mCategoryAdapter.getItem(position);
				int pk = 0;
				if (cur != null) {
					pk = cur.getInt(cur.getColumnIndex(DataProvider.KEY_TOPIC_PK));
				}
				Loge.i("selectItem pk = " + pk);
				DataBaseUtils.updateTopicSelectData(mContext, position, false);
				DataBaseUtils.updateTopicSelectData(mContext, pk, true);
				getLoaderManager().restartLoader(LOADER_ID_LOACL, null, MainActivity.this);
			}
		}).run();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
		switch (loaderID) {
			case LOADER_ID_LOACL: {
				String[] projection = new String[6];
				projection[0] = DataProvider.KEY_TOPIC_ID;
				projection[1] = DataProvider.KEY_TOPIC_NAME;
				projection[2] = DataProvider.KEY_TOPIC_IMAGE_URL;
				projection[3] = DataProvider.KEY_TOPIC_PK;
				projection[4] = DataProvider.KEY_TOPIC_IMAGE_TIME_STAMP;
				projection[5] = DataProvider.KEY_TOPIC_SELECTED;
				return new CursorLoader(this, DataProvider.CONTENT_URI_TOPIC_DATA, projection, null, null,
						DataProvider.KEY_TOPIC_PK);
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
				if (cur != null && cur.getCount() > 0) {
					Loge.i("get data from local count = " + cur.getCount());
					mCategoryAdapter.swapCursor(cur);
					mCategoryAdapter.notifyDataSetChanged();
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
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	class FetchDataTaskNet extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			updating = true;
			if (!Utils.isNetworkAvailable(getBaseContext())) {
				Toast.makeText(MainActivity.this, R.string.no_network, Toast.LENGTH_SHORT).show();
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
					TopicData itemData = new TopicData();
					itemData.mName = "全部";
					itemData.mPk = 0;
					itemData.mSelected = true;
					topicsData.add(0, itemData);

					subItemDatas = CHHNetUtils.getAllSubItemsDate(mContext);
					if (subItemDatas == null) {
						return "fail";
					}
					DataBaseUtils.deleteAllSubItemData(mContext);
					DataBaseUtils.saveSubItemData(mContext, subItemDatas);

					SharedPreferencesUtils.saveUpdateTime(mContext, System.currentTimeMillis());

					if (topicsData != null && topicsData.size() > 0) {
						DataBaseUtils.deleteAllTopicData(mContext);
					}
					DataBaseUtils.saveTopicData(mContext, topicsData);
				} else {
					topicsData = DataBaseUtils.getTopicData(mContext);
					subItemDatas = DataBaseUtils.getSubItemData(mContext);
				}

				if (topicsData == null) {
					return "fail";
				}

				for (TopicData topic : topicsData) {
					Loge.d("TopicData name: " + topic.mName);
					if (topic.mSelected == true) {
						mCategory = topic.mPk;
						break;
					}
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if (mMainDataFrgment != null) {
							mMainDataFrgment.initData(mCategory);
						}
						getLoaderManager().restartLoader(LOADER_ID_LOACL, null, MainActivity.this);
					}
				});

				if (subItemDatas == null) {
					return "fail";
				}

				for (SubItemData sbuitem : subItemDatas) {
					Loge.d("SubItemData name: " + sbuitem.mName);
					Loge.d("SubItemData topic: " + sbuitem.mTopic);
				}

				ArrayList<ContentData> contentDatas = CHHNetUtils.getContentItemsDate(mContext, 1);
				if (contentDatas == null) {
					return "fail";
				}
				Loge.d("contentDatas size: " + contentDatas.size());

				for (int i = contentDatas.size() - 1; i >= 0; i--) {
					ContentData contentItem = contentDatas.get(i);
					for (SubItemData subItemData : subItemDatas) {
						if (contentItem.mSubItemType == subItemData.mPk) {
							contentItem.mTopicType = subItemData.mTopic;
						}
					}
				}

				DataBaseUtils.saveContentItemData(mContext, contentDatas);

				topicsData.clear();
				subItemDatas.clear();
				contentDatas.clear();
				return "success";
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				getLoaderManager().restartLoader(LOADER_ID_LOACL, null, MainActivity.this);
				if (mMainDataFrgment != null) {
					mMainDataFrgment.reloadData(mCategory);
				}
			} else {
				Toast.makeText(mContext, R.string.no_update, Toast.LENGTH_SHORT).show();
			}
			updating = false;
		}
	}

}
