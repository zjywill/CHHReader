package com.comic.chhreader;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.comic.chhreader.data.TopicData;
import com.comic.chhreader.image.PhotoView;

public class MainGridAdapter extends CursorAdapter {

	static class ViewHolder {
		TextView title;
		PhotoView image;
	}

	private Context mContext;
	private Cursor mCursor;

	MainGridAdapter(Context ctx, Cursor cursor) {
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
		Loge.d("Create new view");

		final View itemLayout = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.main_gird_item, null);
		final ViewHolder holder = new ViewHolder();

		holder.image = (PhotoView) itemLayout.findViewById(R.id.gird_image);
		holder.title = (TextView) itemLayout.findViewById(R.id.gird_title);

		itemLayout.setTag(holder);

		return itemLayout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Loge.d("Bind view");

		if (cursor != null) {

			final ViewHolder holder = (ViewHolder) view.getTag();

			TopicData data = new TopicData();

			data.mName = cursor.getString(1);
			data.mImageUrl = cursor.getString(2);

			holder.title.setText(data.mName);

			if (data.mImageUrl == null) {
				return;
			}

			try {
				URL localURL = new URL(data.mImageUrl);
				holder.image.setImageURL(localURL, true, true, null);
				holder.image.setCustomDownloadingImage(R.drawable.gray_image_downloading);
			} catch (MalformedURLException localMalformedURLException) {
				localMalformedURLException.printStackTrace();
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
