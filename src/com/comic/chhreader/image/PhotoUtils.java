package com.comic.chhreader.image;

public class PhotoUtils {
	public static String getPhotoName(String imageUrl) {
		String[] photoFileNameArray = imageUrl.toString().split("/");
		String photoFileName = null;
		if (photoFileNameArray.length > 2) {
			photoFileName = photoFileNameArray[photoFileNameArray.length - 2]
					+ photoFileNameArray[photoFileNameArray.length - 1];
		} else {
			photoFileName = photoFileNameArray[photoFileNameArray.length - 1];
		}
		return photoFileName;
	}
}
