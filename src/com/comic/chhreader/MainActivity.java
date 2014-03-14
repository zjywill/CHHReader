package com.comic.chhreader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.comic.chhreader.content.ContentActivity;
import com.comic.chhreader.data.MainGridData;
import com.comic.chhreader.provider.DataProvider;

public class MainActivity extends Activity implements OnItemClickListener {

	private static final String MAIN_DATA_URL = "ABCD";

	private List<MainGridData> mGridData = new ArrayList<MainGridData>();

	private GridView mGrid;
	private MainGridAdapter mGirdAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mGrid = (GridView) findViewById(R.id.main_gird);
		mGrid.setOnItemClickListener(this);
		mGirdAdapter = new MainGridAdapter(this);
		mGrid.setAdapter(mGirdAdapter);

		new FetchDataTask().execute(MAIN_DATA_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(MainActivity.this, ContentActivity.class);
		intent.putExtra("category", "computer");
		startActivity(intent);
	};

	class FetchDataTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			if (params.length > 0) {
				mGridData.clear();
				String url = params[0];
				Loge.d("Main data url = " + url);

				ContentResolver cr = getContentResolver();

				// first get data from local
				{
					String[] projection = new String[3];
					projection[0] = DataProvider.KEY_MAIN_TITLE;
					projection[1] = DataProvider.KEY_MAIN_PIC_URL;
					projection[2] = DataProvider.KEY_MAIN_SUB_TITLE;

					String selection = DataProvider.KEY_MAIN_CATEGORY + "='" + "main" + "'";
					Loge.d("selection = " + selection);
					Cursor cur = cr.query(DataProvider.CONTENT_URI_MAIN_DATA, projection, selection, null, null);
					if (cur != null) {
						if (cur.moveToFirst()) {
							do {
								MainGridData data = new MainGridData();
								data.mTitle = cur.getString(cur.getColumnIndex(DataProvider.KEY_MAIN_TITLE));
								try {
									data.mPictureUrl = new URL(cur.getString(cur.getColumnIndex(DataProvider.KEY_MAIN_PIC_URL)));
								} catch (MalformedURLException e) {
									e.printStackTrace();
								}
								mGridData.add(data);
							} while (cur.moveToNext());
						}
						cur.close();
					}
				}

				List<MainGridData> tempGridData = new ArrayList<MainGridData>();

				// second get data from net
				String[] titles = getResources().getStringArray(R.array.main_title_array);
				for (String title : titles) {
					Loge.d("Title = " + title);
					MainGridData data = new MainGridData();
					data.mTitle = title;
					try {
						data.mPictureUrl = new URL("http://www.chiphell.com/data/attachment/block/7b/7b7705a60528532b5b9e9b2e36974852.jpg");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					tempGridData.add(data);
				}

				// save data and update data
				if (tempGridData.size() > 0) {
					ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();

					for (MainGridData item : tempGridData) {
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_MAIN_DATA).withValue(DataProvider.KEY_MAIN_TITLE, item.mTitle).withValue(DataProvider.KEY_MAIN_PIC_URL, item.mPictureUrl.toString());
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
				if (mGridData.size() == 0) {
					mGridData.addAll(tempGridData);
				}
				return "success";
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("success")) {
				if (mGridData.size() != 0) {
					mGirdAdapter.setGridData(mGridData);
					mGirdAdapter.notifyDataSetChanged();
				}
			}
		}
	}
}
