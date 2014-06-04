package com.comic.chhreader.utils;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.comic.chhreader.Loge;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.ContentDataDetail;
import com.comic.chhreader.data.SubItemData;
import com.comic.chhreader.data.TopicData;
import com.comic.chhreader.provider.DataProvider;

public class DataBaseUtils {

	public static boolean saveTopicData(Context context,
			ArrayList<TopicData> topicData) {
		if (context != null && topicData != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}

			if (topicData.size() > 0) {
				ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();
				for (TopicData item : topicData) {
					ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_TOPIC_DATA)//
					.withValue(DataProvider.KEY_TOPIC_NAME, item.mName)//
					.withValue(DataProvider.KEY_TOPIC_IMAGE_URL, item.mImageUrl)//
					.withValue(DataProvider.KEY_TOPIC_IMAGE_TIME_STAMP, item.mImageTimeStamp)//
					.withValue(DataProvider.KEY_TOPIC_PK, item.mPk);
					opertions.add(builder.build());
				}
				try {
					contentResolver.applyBatch(DataProvider.DB_AUTHOR, opertions);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	public static boolean updateTopicImage(Context context, int topic,
			String imageUrl) {
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}

			ContentValues contentValues = new ContentValues();
			contentValues.put(DataProvider.KEY_TOPIC_IMAGE_URL, imageUrl);

			String where = DataProvider.KEY_TOPIC_PK + "='" + topic + "'";
			contentResolver.update(DataProvider.CONTENT_URI_TOPIC_DATA, contentValues, where, null);
			return true;
		}
		return false;
	}

	public static boolean deleteAllTopicData(Context context) {
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}
			contentResolver.delete(DataProvider.CONTENT_URI_TOPIC_DATA, null, null);
			return true;
		}
		return false;
	}

	public static ArrayList<TopicData> getTopicData(Context context) {
		ArrayList<TopicData> topicData = null;
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return topicData;
			}
			Cursor cursor = contentResolver.query(DataProvider.CONTENT_URI_TOPIC_DATA, null, null, null, null);

			if (cursor != null) {
				if (cursor.getCount() > 0) {
					if (cursor.moveToFirst()) {
						do {
							TopicData itemData = new TopicData();
							itemData.mName = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_TOPIC_NAME));
							itemData.mPk = cursor.getInt(cursor.getColumnIndex(DataProvider.KEY_TOPIC_PK));
							itemData.mImageUrl = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_TOPIC_IMAGE_URL));
							itemData.mImageTimeStamp = cursor.getLong(cursor.getColumnIndex(DataProvider.KEY_TOPIC_IMAGE_TIME_STAMP));
							if (topicData == null) {
								topicData = new ArrayList<TopicData>();
							}
							topicData.add(itemData);
						} while (cursor.moveToNext());
					}
				}
				cursor.close();
			}

			return topicData;
		}
		return topicData;
	}

	public static boolean isTopicDataExist(Context context) {
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}
			Cursor cursor = contentResolver.query(DataProvider.CONTENT_URI_TOPIC_DATA, null, null, null, null);
			if (cursor == null) {
				return false;
			}
			int count = cursor.getCount();
			cursor.close();
			return count > 0 ? true : false;
		}
		return false;

	}

	public static boolean saveSubItemData(Context context,
			ArrayList<SubItemData> subItemDatas) {
		if (context != null && subItemDatas != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}

			if (subItemDatas.size() > 0) {
				ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();
				for (SubItemData item : subItemDatas) {
					ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DataProvider.CONTENT_URI_SUBITEM_DATA)//
					.withValue(DataProvider.KEY_SUBITEM_NAME, item.mName)//
					.withValue(DataProvider.KEY_SUBITEM_URL, item.mUrl)//
					.withValue(DataProvider.KEY_SUBITEM_PK, item.mPk)//
					.withValue(DataProvider.KEY_SUBITEM_TOPIC_PK, item.mTopic);
					opertions.add(builder.build());
				}
				try {
					contentResolver.applyBatch(DataProvider.DB_AUTHOR, opertions);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	public static ArrayList<SubItemData> getSubItemData(Context context) {
		ArrayList<SubItemData> subItemDatas = null;
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return subItemDatas;
			}
			Cursor cursor = contentResolver.query(DataProvider.CONTENT_URI_SUBITEM_DATA, null, null, null, null);

			if (cursor != null) {
				if (cursor.getCount() > 0) {
					if (cursor.moveToFirst()) {
						do {
							SubItemData itemData = new SubItemData();
							itemData.mName = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_SUBITEM_NAME));
							itemData.mPk = cursor.getInt(cursor.getColumnIndex(DataProvider.KEY_SUBITEM_PK));
							itemData.mTopic = cursor.getInt(cursor.getColumnIndex(DataProvider.KEY_SUBITEM_TOPIC_PK));
							itemData.mUrl = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_SUBITEM_URL));
							if (subItemDatas == null) {
								subItemDatas = new ArrayList<SubItemData>();
							}
							subItemDatas.add(itemData);
						} while (cursor.moveToNext());
					}
				}
				cursor.close();
			}

			return subItemDatas;
		}
		return subItemDatas;
	}

	public static boolean deleteAllSubItemData(Context context) {
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}
			contentResolver.delete(DataProvider.CONTENT_URI_SUBITEM_DATA, null, null);
			return true;
		}
		return false;
	}

	public static boolean saveContentItemData(Context context,
			ArrayList<ContentData> contentItemDatas) {
		if (context != null && contentItemDatas != null) {
			Loge.d("saveContentItemData size:  " + contentItemDatas.size());
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}

			if (contentItemDatas.size() > 0) {
				ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();
				for (ContentData item : contentItemDatas) {
					String selection = DataProvider.KEY_MAIN_URL + "='" + item.mLink + "'";
					ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(DataProvider.CONTENT_URI_MAIN_DATA)//
					.withValue(DataProvider.KEY_MAIN_TITLE, item.mTitle)//
					.withValue(DataProvider.KEY_MAIN_URL, item.mLink)//
					.withValue(DataProvider.KEY_MAIN_PIC_URL, item.mImageUrl)//
					.withValue(DataProvider.KEY_MAIN_SUB_PK, item.mSubItemType)//
					.withValue(DataProvider.KEY_MAIN_TOPIC_PK, item.mTopicType)//
					.withValue(DataProvider.KEY_MAIN_CONTENT, item.mContent)//
					.withValue(DataProvider.KEY_MAIN_VALID, item.mValid ? 1 : 0)//
					.withValue(DataProvider.KEY_MAIN_PUBLISH_DATE, item.mPostDate)//
					.withSelection(selection, null);
					opertions.add(builder.build());
				}
				try {
					contentResolver.applyBatch(DataProvider.DB_AUTHOR, opertions);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	public static boolean deleteAllContentItemData(Context context) {
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}
			contentResolver.delete(DataProvider.CONTENT_URI_MAIN_DATA, null, null);
			return true;
		}
		return false;
	}

	public static boolean updateContentData(Context context, String url,
			String body, String imageset) {
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return false;
			}
			ContentValues cv = new ContentValues();
			cv.put(DataProvider.KEY_CONTENT_URL, url);
			cv.put(DataProvider.KEY_CONTENT_BODY, body);
			cv.put(DataProvider.KEY_CONTENT_UPLOAD_DATE, System.currentTimeMillis());
			cv.put(DataProvider.KEY_CONTENT_IMAGE_SET, imageset);
			String where = DataProvider.KEY_CONTENT_URL + "='" + url + "'";
			contentResolver.update(DataProvider.CONTENT_URI_CONTENT_DATA, cv, where, null);
			return true;
		}
		return false;
	}

	public static ContentDataDetail getContentData(Context context, String url) {
		if (context != null) {
			ContentDataDetail data = null;
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver == null) {
				return data;
			}
			String where = DataProvider.KEY_CONTENT_URL + "='" + url + "'";
			Cursor cursor = contentResolver.query(DataProvider.CONTENT_URI_CONTENT_DATA, null, where, null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					if (cursor.moveToFirst()) {
						data = new ContentDataDetail();
						data.mBody = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_CONTENT_BODY));
						data.mImageSet = cursor.getString(cursor.getColumnIndex(DataProvider.KEY_CONTENT_IMAGE_SET));
						data.mUpdateDate = cursor.getLong(cursor.getColumnIndex(DataProvider.KEY_CONTENT_UPLOAD_DATE));
					}
				}
				cursor.close();
			}
			return data;
		}
		return null;
	}

}
