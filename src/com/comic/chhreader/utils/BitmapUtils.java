package com.comic.chhreader.utils;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.comic.chhreader.Loge;

public class BitmapUtils {

	public static final int TARGET_PHOTO_WIDTH = 1080;

	public static Bitmap compressBitmap(File originBitmap) {

		Bitmap compressedBitmap = null;

		if (originBitmap != null) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;

			BitmapFactory.decodeFile(originBitmap.getPath(), opts);

			int oriW = opts.outWidth;
			int oriH = opts.outHeight;
			Loge.i("origin Width = " + oriW);

			int sampleSize = (int) (oriW / TARGET_PHOTO_WIDTH);

			Loge.i("sampleSize = " + sampleSize);

			if (sampleSize < 1) {
				sampleSize = 1;
			}

			opts.inSampleSize = sampleSize;
			opts.inJustDecodeBounds = false;
			opts.inPurgeable = true;

			compressedBitmap = BitmapFactory.decodeFile(originBitmap.getPath(),
					opts);
		}

		Bitmap finalBitmap = null;
		if (compressedBitmap != null) {
			finalBitmap = cropBitmap(compressedBitmap);
		}
		if (finalBitmap == null) {
			return compressedBitmap;
		} else {
			compressedBitmap.recycle();
			compressedBitmap = null;
			return finalBitmap;
		}
	}

	public static Bitmap cropBitmap(Bitmap oriBitmap) {
		if (oriBitmap == null) {
			return null;
		}
		int oriW = oriBitmap.getWidth();
		int oriH = oriBitmap.getHeight();

		Bitmap cropedBitmap = null;
		if (oriW != oriH) {
			if (oriW > oriH) {
				cropedBitmap = Bitmap.createBitmap(oriBitmap,
						(oriW - oriH) / 2, 0, oriH, oriH);
			} else {
				cropedBitmap = Bitmap.createBitmap(oriBitmap, 0,
						(oriH - oriW) / 2, oriW, oriW);
			}
		}

		return cropedBitmap;
	}

	public static File getBitmapFileFromUri(Context ctx, Uri uri) {
		ContentResolver contentResolver = ctx.getContentResolver();
		File bitmapFile = null;
		if (contentResolver != null) {
			Cursor cursor = MediaStore.Images.Media.query(contentResolver, uri,
					null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String filePath = cursor.getString(1);
				Loge.i("Photo file path: " + filePath);
				bitmapFile = new File(filePath);
				cursor.close();
			}
		}

		return bitmapFile;
	}

}
