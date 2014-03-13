package com.comic.chhreader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.comic.chhreader.data.MainGridData;
import com.comic.chhreader.image.PhotoView;

public class MainGridAdapter extends BaseAdapter {

	private List<MainGridData> mGridData = new ArrayList<MainGridData>();
	private Context mContext;
	private Drawable mDefaultDrawable;

	static class ViewHolder {
		TextView title;
		PhotoView image;
		TextView content;
	}

	MainGridAdapter(Context context) {
		mContext = context;
		mDefaultDrawable = mContext.getResources().getDrawable(R.drawable.gray_image_downloading);
	}

	@Override
	public int getCount() {
		Loge.d("getCount = " + mGridData.size());
		return mGridData.size();
	}

	@Override
	public MainGridData getItem(int position) {
		return mGridData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.main_gird_item, null);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.gird_title);
			holder.image = (PhotoView) convertView.findViewById(R.id.gird_image);
			holder.content = (TextView) convertView.findViewById(R.id.gird_content);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		synchronized (mGridData) {
			MainGridData itemData = mGridData.get(position);
			if (itemData != null) {
				Loge.d("getView title = " + itemData.mTitle);
				holder.title.setText(itemData.mTitle);
				holder.image.setImageURL(itemData.mPictureUrl, false, true, mDefaultDrawable);
				holder.content.setText(itemData.mContent);
			}
		}

		return convertView;
	}

	public void setGridData(List<MainGridData> gridData) {
		synchronized (mGridData) {
			mGridData.clear();
			mGridData.addAll(gridData);
			Loge.d("setGridData size = " + mGridData.size());
		}
	}

}
