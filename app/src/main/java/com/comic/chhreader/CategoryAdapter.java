package com.comic.chhreader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.comic.chhreader.data.TopicData;
import com.comic.chhreader.provider.DataProvider;

public class CategoryAdapter extends CursorAdapter {

	static class ViewHolder {
		TextView title;
	}

	private Context mContext;
	private Cursor mCursor;

	CategoryAdapter(Context ctx, Cursor cursor) {
		super(ctx, cursor, 0);
		mContext = ctx;
		mCursor = cursor;
	}

	void setCursor(Cursor cursor) {
		setCursor(cursor);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		mCursor = newCursor;
		return super.swapCursor(newCursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View itemLayout = LayoutInflater.from(mContext.getApplicationContext()).inflate(
				R.layout.drawer_list_item, null);
		final ViewHolder holder = new ViewHolder();

		holder.title = (TextView) itemLayout.findViewById(R.id.text_title);

		itemLayout.setTag(holder);

		return itemLayout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		if (cursor != null) {

			final ViewHolder holder = (ViewHolder) view.getTag();

			TopicData data = new TopicData();

			data.mName = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_TOPIC_NAME));
			data.mSelected = cursor.getInt(cursor.getColumnIndex(DataProvider.KEY_TOPIC_SELECTED)) == 1;

			holder.title.setText(data.mName);

			if (data.mSelected) {
				holder.title.setTextSize(22.33f);
				holder.title.setTypeface(Typeface.DEFAULT_BOLD);
			} else {
				holder.title.setTextSize(19.33f);
				holder.title.setTypeface(Typeface.DEFAULT);
			}
		}
	}

	@Override
	public int getCount() {
		if (getCursor() == null) {
			return 0;
		}
		int count = getCursor().getCount();
		return count;
	}

	@Override
	public Object getItem(int position) {
		if (mCursor != null) {
			mCursor.moveToPosition(position);
			return mCursor;
		} else {
			return null;
		}
	}

}
