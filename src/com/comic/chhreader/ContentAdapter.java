package com.comic.chhreader;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.imageloader.ImageCacheManager;

public class ContentAdapter extends SimpleCursorAdapter {

	public static final String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/Android/data/com.comic.chhreader/Images/ImageLoader";

	static class ViewHolder {
		NetworkImageView icon;
		TextView title;
		TextView subcontent;
		LinearLayout section;
		TextView section_text;
	}

	private Context mContext;
	private Cursor mDataCursor;
	private LayoutInflater mInflater;
	private boolean mNoImage = false;
	private long mToday;

	public ContentAdapter(Context context) {
		super(context, 0, null, null, null);
		mContext = context;
		mDataCursor = null;
		mInflater = LayoutInflater.from(context);
		mToday = System.currentTimeMillis();
	}

	void setCursor(Cursor cursor) {
		mDataCursor = cursor;
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		mDataCursor = newCursor;
		return newCursor;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.content_list_item, null);
			holder = new ViewHolder();
			holder.icon = (NetworkImageView) convertView.findViewById(R.id.content_ori_image);
			holder.title = (TextView) convertView.findViewById(R.id.content_title_text);
			holder.subcontent = (TextView) convertView.findViewById(R.id.content_subcontent_text);
			holder.section = (LinearLayout) convertView.findViewById(R.id.list_section);
			holder.section_text = (TextView) convertView.findViewById(R.id.list_section_text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (mDataCursor != null) {
			mDataCursor.moveToPosition(position);

			ContentData data = new ContentData();

			data.mTitle = mDataCursor.getString(1);
			data.mImageUrl = mDataCursor.getString(2);
			data.mContent = mDataCursor.getString(3);
			data.mPostDate = mDataCursor.getLong(4);

			holder.title.setText(data.mTitle);
			holder.subcontent.setText(data.mContent);

			if (data.mImageUrl != null) {
				holder.icon.setImageUrl(data.mImageUrl, ImageCacheManager.getInstance().getImageLoader());
			}

			long prevDate = 0;
			if (mDataCursor.getPosition() > 0 && mDataCursor.moveToPrevious()) {
				prevDate = mDataCursor.getLong(4);
				mDataCursor.moveToNext();
			}

			if (isSameDate(prevDate, data.mPostDate)) {
				holder.section.setVisibility(View.GONE);
			} else {
				holder.section.setVisibility(View.VISIBLE);
				if (position == 0) {
					holder.section_text.setText("最新");
				} else {
					holder.section_text.setText(DateUtils.formatDateTime(mContext, data.mPostDate * 1000,
							DateUtils.FORMAT_24HOUR));
				}
			}
		}

		return convertView;
	}

	public static boolean isSameDate(long date1, long date2) {
		long days1 = date1 / (60 * 60 * 24);
		long days2 = date2 / (60 * 60 * 24);
		return days1 == days2;
	}

	@Override
	public int getCount() {
		if (mDataCursor == null) {
			return 0;
		}
		int count = mDataCursor.getCount();
		return count;
	}

	@Override
	public Object getItem(int position) {
		Cursor cursor = mDataCursor;
		if (cursor != null) {
			if (position >= cursor.getCount()) {
				return null;
			}
			cursor.moveToPosition(position);
			return cursor;
		} else {
			return null;
		}
	}

}
