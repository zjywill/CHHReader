package com.comic.chhreader;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.comic.chhreader.content.ContentActivity;
import com.comic.chhreader.data.MainGridData;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.Utils;
import com.comic.chhreader.view.NetworkDialog;

public class MainActivity extends Activity implements OnItemClickListener, LoaderCallbacks<Cursor> {

	private static final int LOADER_ID_LOACL = 103;
	private static final String MAIN_DATA_URL = "ABCD";

	private GridView mGrid;
	private MainGridAdapter mGirdAdapter;

	private ProgressBar mLoadingProgress;
	private ImageButton mRefreshBtn;

	private int mShortAnimationDuration;

	private Context mContext = this;
	private NetworkDialog mNetworkDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mGrid = (GridView) findViewById(R.id.main_gird);
		mGrid.setOnItemClickListener(this);
		mGirdAdapter = new MainGridAdapter(this, null);
		mGrid.setAdapter(mGirdAdapter);

		initActionBar();
		mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

		mGrid.setAlpha(0f);
		mGrid.animate().alpha(1f).setDuration(800).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mGrid.setVisibility(View.VISIBLE);
			}
		});

		getLoaderManager().initLoader(LOADER_ID_LOACL, null, this);

		// new FetchDataTaskLocal().execute();
	}

	void initActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {

			actionBar.setIcon(R.drawable.title_icon);

			Loge.i("action bar setCustomView");
			LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View actionbarView = inflator.inflate(R.layout.main_activity_actionbar, null);
			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			actionBar.setCustomView(actionbarView, lp);
			actionBar.setDisplayShowCustomEnabled(true);

			mRefreshBtn = (ImageButton) actionbarView.findViewById(R.id.action_refreash);
			mLoadingProgress = (ProgressBar) actionbarView.findViewById(R.id.action_loading);

			mRefreshBtn.setOnClickListener(mRefreshClicked);
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
		case R.id.action_settings: {
			Loge.i("Options Selected = settings");
		}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private View.OnClickListener mRefreshClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Loge.i("Refresh Btn clicked");
			new FetchDataTaskNet().execute();
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(MainActivity.this, ContentActivity.class);
		Cursor cur = (Cursor) mGirdAdapter.getItem(position);
		if (cur != null) {
			intent.putExtra("title", cur.getString(1));
			intent.putExtra("category", cur.getString(4));
			startActivity(intent);
		}
	};

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		Loge.i("onCreateLoader");
		switch (loaderID) {
		case LOADER_ID_LOACL: {
			String[] projection = new String[5];
			projection[0] = DataProvider.KEY_MAIN_ID;
			projection[1] = DataProvider.KEY_MAIN_TITLE;
			projection[2] = DataProvider.KEY_MAIN_PIC_URL;
			projection[3] = DataProvider.KEY_MAIN_TYPE;
			projection[4] = DataProvider.KEY_MAIN_CATEGORY;

			String selection = DataProvider.KEY_MAIN_TYPE + "='" + "main" + "'";
			Loge.d("selection = " + selection);
			return new CursorLoader(this, DataProvider.CONTENT_URI_MAIN_DATA, projection, selection, null, null);
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

	private void showContentOrLoadingIndicator(boolean contentLoaded) {
		// Decide which view to hide and which to show.
		final View showView = contentLoaded ? mLoadingProgress : mRefreshBtn;
		final View hideView = contentLoaded ? mRefreshBtn : mLoadingProgress;

		// Set the "show" view to 0% opacity but visible, so that it is visible
		// (but fully transparent) during the animation.
		showView.setAlpha(0f);
		showView.setVisibility(View.VISIBLE);

		// Animate the "show" view to 100% opacity, and clear any animation
		// listener set on
		// the view. Remember that listeners are not limited to the specific
		// animation
		// describes in the chained method calls. Listeners are set on the
		// ViewPropertyAnimator object for the view, which persists across
		// several
		// animations.
		showView.animate().alpha(1f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				showView.setVisibility(View.VISIBLE);
			}
		});

		// Animate the "hide" view to 0% opacity. After the animation ends, set
		// its visibility
		// to GONE as an optimization step (it won't participate in layout
		// passes, etc.)
		hideView.animate().alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				hideView.setVisibility(View.GONE);
			}
		});
	}

	//
	// class FetchDataTaskLocal extends AsyncTask<Void, Void, Cursor> {
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// }
	//
	// @Override
	// protected Cursor doInBackground(Void... params) {
	// mGridData.clear();
	// ContentResolver cr = getContentResolver();
	//
	// // get data from local
	// String[] projection = new String[5];
	// projection[0] = DataProvider.KEY_MAIN_ID;
	// projection[1] = DataProvider.KEY_MAIN_TITLE;
	// projection[2] = DataProvider.KEY_MAIN_PIC_URL;
	// projection[3] = DataProvider.KEY_MAIN_TYPE;
	// projection[4] = DataProvider.KEY_MAIN_CATEGORY;
	//
	// String selection = DataProvider.KEY_MAIN_TYPE + "='" + "main" + "'";
	// Loge.d("selection = " + selection);
	// Cursor cur = cr.query(DataProvider.CONTENT_URI_MAIN_DATA, projection,
	// selection, null, null);
	// return cur;
	// }
	//
	// @Override
	// protected void onPostExecute(Cursor cur) {
	// super.onPostExecute(cur);
	// if (cur != null && cur.getCount() > 0) {
	// Loge.i("get data from local count = " + cur.getCount());
	// mGirdAdapter.swapCursor(cur);
	// mGirdAdapter.notifyDataSetChanged();
	// } else {
	// Loge.i("Cursor is null or count == 0");
	// new FetchDataTaskNet().execute();
	// }
	// }
	// }

	class FetchDataTaskNet extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showContentOrLoadingIndicator(true);
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
				ContentResolver cr = getContentResolver();

				List<MainGridData> tempGridData = new ArrayList<MainGridData>();

				CHHNetUtils.getTopicsDate(mContext);

				for (int i = 0; i < 8; i++) {
					CHHNetUtils.getSubItemsDate(mContext, i);
				}

				// get data from net
				String[] titles = getResources().getStringArray(R.array.main_title_array);
				String[] types = getResources().getStringArray(R.array.main_type_array);
				int i = 0;
				for (String title : titles) {
					MainGridData data = new MainGridData();
					data.mTitle = title;
					data.mPictureUrl = "http://www.chiphell.com/data/attachment/portal/201404/11/120705xe97lrraey60z99r.jpg";
					data.mType = "main";
					data.mCategory = types[i];
					i++;
					tempGridData.add(data);
				}

				// save data and update data
				if (tempGridData.size() > 0) {
					ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();
					for (MainGridData item : tempGridData) {
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_MAIN_DATA).withValue(DataProvider.KEY_MAIN_TITLE, item.mTitle).withValue(DataProvider.KEY_MAIN_PIC_URL, item.mPictureUrl).withValue(DataProvider.KEY_MAIN_TYPE, item.mType).withValue(DataProvider.KEY_MAIN_CATEGORY, item.mCategory);
						opertions.add(builder.build());
					}
					try {
						cr.applyBatch(DataProvider.DB_AUTHOR, opertions);
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
			showContentOrLoadingIndicator(false);
			if (result.equals("success")) {
				getLoaderManager().restartLoader(LOADER_ID_LOACL, null, MainActivity.this);
			}
		}

	}
}
