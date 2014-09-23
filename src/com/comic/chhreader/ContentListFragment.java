package com.comic.chhreader;

import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.comic.chhreader.detail.DetailActivity;
import com.comic.chhreader.provider.DataProvider;

public class ContentListFragment extends SwipeRefreshListFragment implements LoaderCallbacks<Cursor>,
		OnItemClickListener {

	public static final int LOADER_ID_LOACL = 105;

	private ContentAdapter mListAdapter;

	private int mCategory = 0;

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

	}

	public void initData(int category) {
		mCategory = category;
		getLoaderManager().initLoader(LOADER_ID_LOACL, null, ContentListFragment.this);
	}

	public void reloadData(int category) {
		mCategory = category;
		getLoaderManager().restartLoader(LOADER_ID_LOACL, null, ContentListFragment.this);
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
				if (cur != null && cur.getCount() > 0) {
					Loge.i("get data from local count = " + cur.getCount());
					mListAdapter.swapCursor(cur);
					mListAdapter.notifyDataSetChanged();
					setRefreshing(false);
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

	private void onRefreshComplete(List<String> result) {
		setRefreshing(false);
	}

	private class GetContentDataTask extends AsyncTask<Void, Void, String> {

		static final int TASK_DURATION = 3 * 1000; // 3 seconds

		@Override
		protected String doInBackground(Void... params) {
			// Sleep for a small amount of time to simulate a background-task
			try {
				Thread.sleep(TASK_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Return a new random list of cheeses
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}
}
