package com.comic.chhreader.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PhotoView extends ImageView implements RemoteImageLoaderListener {

	public PhotoView(Context context) {
		super(context);
	}

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setImageURL(String imageurl, String downloadPath) {
		if (imageurl != null && downloadPath != null) {
			PhotoManager.startDownload(imageurl, downloadPath, this);
		}

	}

	public void setCustomDownloadingImage(int resId) {
		setImageResource(resId);
	}

	@Override
	public void onDownloadStart(String url) {

	}

	@Override
	public void onDownloadComplete(String url, Bitmap images) {
		setImageBitmap(images);
	}

	@Override
	public void onCacheLoaded(String path, Bitmap images) {
		setImageBitmap(images);
	}

	@Override
	public void onDownloadFailed() {

	}

}
