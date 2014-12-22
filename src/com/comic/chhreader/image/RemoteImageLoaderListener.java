package com.comic.chhreader.image;

import android.graphics.Bitmap;

public interface RemoteImageLoaderListener {
	void onDownloadStart(String url);

	void onDownloadComplete(String url, Bitmap images);
	
	void onCacheLoaded(String path, Bitmap images);

	void onDownloadFailed();
}
