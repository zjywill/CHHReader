package com.comic.chhreader.content;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.comic.chhreader.Loge;
import com.comic.chhreader.R;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.image.PhotoView;

public class ContentAdapter extends CursorAdapter {

	static class ViewHolder {
		PhotoView icon;
		TextView title;
		TextView subcontent;
	}

	private Context mContext;

	ContentAdapter(Context ctx) {
		super(ctx, null, 0);
		mContext = ctx;

	}

	void setCursor(Cursor cursor) {
		setCursor(cursor);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		Cursor cursor = getCursor();
		if (cursor != null) {
			cursor.close();
		}
		return super.swapCursor(newCursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View itemLayout = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.content_list_item, null);
		final ViewHolder holder = new ViewHolder();

		holder.icon = (PhotoView) itemLayout.findViewById(R.id.content_ori_image);
		holder.title = (TextView) itemLayout.findViewById(R.id.content_title_text);
		holder.subcontent = (TextView) itemLayout.findViewById(R.id.content_subcontent_text);

		itemLayout.setTag(holder);

		return itemLayout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			final ViewHolder holder = (ViewHolder) view.getTag();

			ContentData data = new ContentData();

			data.mTitle = cursor.getString(1);
			data.mImageUrl = cursor.getString(2);
			data.mContent = cursor.getString(3);
			data.mPostDate = cursor.getLong(4);

			holder.title.setText(data.mTitle);
			holder.subcontent.setText(data.mContent);

			if (data.mImageUrl == null) {
				return;
			}

			try {
				URL localURL = new URL(data.mImageUrl);
				holder.icon.setImageURL(localURL, true, true, null);
				holder.icon.setCustomDownloadingImage(R.drawable.gray_image_downloading);
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
		Cursor cursor = getCursor();
		if (cursor != null) {
			cursor.moveToPosition(position);
			return cursor;
		} else {
			return null;
		}
	}

}
