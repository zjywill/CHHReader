package com.comic.chhreader.content;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.detail.DetailActivity;
import com.comic.chhreader.provider.DataProvider;
import com.comic.chhreader.utils.SharedPreferencesUtils;
import com.comic.chhreader.utils.Utils;

public class FavorActivity extends Activity implements OnItemClickListener, LoaderCallbacks<Cursor> {

	private static final int LOADER_ID_LOACL = 106;

	private Context mCtx;

	private ListView mListView;
	private ContentAdapter mListAdapter;

	private boolean mNoImage = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCtx = this;
		setContentView(R.layout.favor_activity);

		mListView = (ListView) findViewById(R.id.favor_list);

		mListAdapter = new ContentAdapter(this);

		if (SharedPreferencesUtils.getNoImageMode(mCtx) && !Utils.isWifiAvailable(mCtx)) {
			mNoImage = true;
		} else {
			mNoImage = false;
		}

		mListAdapter.setNoImage(mNoImage);

		mListView.setDivider(null);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);

		intActionBar();
	}

	void intActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(R.string.action_myfavor);
			actionBar.setIcon(R.drawable.chh_icon);
		}
	}

	@Override
	protected void onResume() {
		getLoaderManager().initLoader(LOADER_ID_LOACL, null, this);
		super.onResume();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		switch (loaderID) {
			case LOADER_ID_LOACL: {
				String[] projection = new String[6];
				projection[0] = DataProvider.KEY_MAIN_ID;
				projection[1] = DataProvider.KEY_MAIN_TITLE;
				projection[2] = DataProvider.KEY_MAIN_PIC_URL;
				projection[3] = DataProvider.KEY_MAIN_CONTENT;
				projection[4] = DataProvider.KEY_MAIN_PUBLISH_DATE;
				projection[5] = DataProvider.KEY_MAIN_URL;

				String selection = DataProvider.KEY_MAIN_FAVOR + "='" + "1" + "'"//
						+ " AND " + DataProvider.KEY_MAIN_VALID + "='" + "1" + "'";
				Loge.i("selection = " + selection);

				return new CursorLoader(this, DataProvider.CONTENT_URI_MAIN_DATA, projection, selection,
						null, DataProvider.KEY_MAIN_PUBLISH_DATE + " DESC");
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
				mListAdapter.swapCursor(cur);
				mListAdapter.notifyDataSetChanged();
			}
				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(FavorActivity.this, DetailActivity.class);
		Cursor cur = (Cursor) mListAdapter.getItem(position);
		if (cur != null) {
			intent.putExtra("title", cur.getString(1));
			intent.putExtra("url", cur.getString(5));
			startActivity(intent);
		}
	}

}
