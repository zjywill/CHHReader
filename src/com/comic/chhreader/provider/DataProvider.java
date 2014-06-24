package com.comic.chhreader.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.comic.chhreader.Loge;

public class DataProvider extends ContentProvider {

	private static final String DB_NAME = "chhdatadb.db";
	private static final int DB_VERSION = 1004;
	public static final String DB_AUTHOR = "com.comic.chhreader";
	private DBHelper mOpenHelper;

	public static final Uri CONTENT_URI_TOPIC_DATA = Uri.parse("content://com.comic.chhreader/topic");
	public static final Uri CONTENT_URI_SUBITEM_DATA = Uri.parse("content://com.comic.chhreader/subitem");
	public static final Uri CONTENT_URI_MAIN_DATA = Uri.parse("content://com.comic.chhreader/main");
	public static final Uri CONTENT_URI_CONTENT_DATA = Uri.parse("content://com.comic.chhreader/content");
	public static final Uri CONTENT_URI_NEWS_DATA = Uri.parse("content://com.comic.chhreader/news");

	// Name of table in the database
	private static final String DB_TABLE_TOPIC_DATA = "topic";

	// main table topic keys
	public static final String KEY_TOPIC_ID = "_id";
	public static final String KEY_TOPIC_NAME = "name";
	public static final String KEY_TOPIC_IMAGE_URL = "imageurl";
	public static final String KEY_TOPIC_IMAGE_TIME_STAMP = "imagetime";
	public static final String KEY_TOPIC_PK = "pk";

	// Name of table in the database
	private static final String DB_TABLE_SUBITEM_DATA = "subitem";

	// main table topic keys
	public static final String KEY_SUBITEM_ID = "_id";
	public static final String KEY_SUBITEM_NAME = "name";
	public static final String KEY_SUBITEM_PK = "pk";
	public static final String KEY_SUBITEM_TOPIC_PK = "topic";
	public static final String KEY_SUBITEM_URL = "url";

	// Name of table in the database
	private static final String DB_TABLE_MAIN_DATA = "main";

	// main table data keys
	public static final String KEY_MAIN_ID = "_id";
	public static final String KEY_MAIN_TITLE = "title";
	public static final String KEY_MAIN_PIC_URL = "picture";
	public static final String KEY_MAIN_TOPIC_PK = "topicpk";
	public static final String KEY_MAIN_SUB_PK = "subitempk";
	public static final String KEY_MAIN_POSTER = "poster";
	public static final String KEY_MAIN_CONTENT = "content";
	public static final String KEY_MAIN_URL = "url";
	public static final String KEY_MAIN_EXTEND_DATA1 = "extend1";
	public static final String KEY_MAIN_EXTEND_DATA2 = "extend2";
	public static final String KEY_MAIN_VALID = "valid";
	public static final String KEY_MAIN_PUBLISH_DATE = "date";
	public static final String KEY_MAIN_FAVOR = "favorite";

	// Name of table in the database
	private static final String DB_TABLE_CONTENT_DATA = "content";

	// content data table
	public static final String KEY_CONTENT_ID = "_id";
	public static final String KEY_CONTENT_URL = "url";
	public static final String KEY_CONTENT_BODY = "body";
	public static final String KEY_CONTENT_UPLOAD_DATE = "time";
	public static final String KEY_CONTENT_IMAGE_SET = "imageset";
	public static final String KEY_CONTENT_ORIGIN = "origincontent";
	public static final String KEY_CONTENT_FAVOR = "favorite";
	
	// Name of table in the database
	private static final String DB_TABLE_NEWS_DATA = "news";

	// news data table
	public static final String KEY_NEWS_ID = "_id";
	public static final String KEY_NEWS_TITLE = "title";
	public static final String KEY_NEWS_URL = "url";
	public static final String KEY_NEWS_IMAGE_URL = "imageurl";
	public static final String KEY_NEWS_DESCRIPTION = "description";
	public static final String KEY_NEWS_UPLOAD_DATE = "time";

	@Override
	public boolean onCreate() {
		Loge.d("Create DataBase CHH Data");
		Context context = getContext();
		mOpenHelper = new DBHelper(context);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(getTable(uri));

		String orderBy;
		if (uri.equals(CONTENT_URI_MAIN_DATA) && TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_MAIN_PUBLISH_DATE;
		} else {
			orderBy = sortOrder;
		}

		Cursor c = null;

		try {
			c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
			c.setNotificationUri(getContext().getContentResolver(), uri);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		try {
			db.insert(table_name, KEY_MAIN_CONTENT, values);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		int count = 0;
		try {
			count = db.delete(table_name, selection, selectionArgs);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		long count = 0;
		try {
			count = db.update(table_name, values, selection, selectionArgs);
			if (count <= 0) {
				count = db.insert(table_name, KEY_MAIN_CONTENT, values);
			}
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return (int) count;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/vnd.comic.chhreader";
	}

	private String getTable(Uri uri) {
		String sUri = uri.toString();
		if (sUri.equals(CONTENT_URI_MAIN_DATA.toString())) {
			return DB_TABLE_MAIN_DATA;
		} else if (sUri.equals(CONTENT_URI_TOPIC_DATA.toString())) {
			return DB_TABLE_TOPIC_DATA;
		} else if (sUri.equals(CONTENT_URI_SUBITEM_DATA.toString())) {
			return DB_TABLE_SUBITEM_DATA;
		} else if (sUri.equals(CONTENT_URI_CONTENT_DATA.toString())) {
			return DB_TABLE_CONTENT_DATA;
		} else if (sUri.equals(CONTENT_URI_NEWS_DATA.toString())) {
			return DB_TABLE_NEWS_DATA;
		}
		return "";
	}

	public class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Loge.w("onUpgrade oldVersion: " + oldVersion + " newVersion: " + newVersion);
			switch (newVersion) {
				case 1002: {
					addColumn(db, DB_TABLE_TOPIC_DATA, KEY_TOPIC_IMAGE_TIME_STAMP,
							"INTEGER NOT NULL DEFAULT 0");
					addColumn(db, DB_TABLE_CONTENT_DATA, KEY_CONTENT_IMAGE_SET, "TEXT");
				}
				case 1003: {
					addColumn(db, DB_TABLE_CONTENT_DATA, KEY_CONTENT_ORIGIN, "TEXT");
					addColumn(db, DB_TABLE_CONTENT_DATA, KEY_CONTENT_FAVOR,
							"INTEGER NOT NULL DEFAULT 0");
					createNewsTable(db);
				}
				case 1004: {
					addColumn(db, DB_TABLE_MAIN_DATA, KEY_MAIN_FAVOR,
							"INTEGER NOT NULL DEFAULT 0");
				}
				break;
				default: {
					db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_TOPIC_DATA);
					db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_SUBITEM_DATA);
					db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_MAIN_DATA);
					db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CONTENT_DATA);

					createTable(db);
				}
					break;
			}
		}

		private void createTable(SQLiteDatabase db) {
			String commandTopic = "create table " + DB_TABLE_TOPIC_DATA //
					+ " (" + KEY_TOPIC_ID + " integer primary key autoincrement, " //
					+ KEY_TOPIC_NAME + " TEXT," + KEY_TOPIC_IMAGE_URL + " TEXT," //
					+  KEY_TOPIC_PK+ " INTEGER, "//
					+  KEY_TOPIC_IMAGE_TIME_STAMP + " INTEGER );";

			String commandSubitem = "create table "
					+ DB_TABLE_SUBITEM_DATA //
					+ " (" + KEY_SUBITEM_ID
					+ " integer primary key autoincrement, " //
					+ KEY_SUBITEM_NAME + " TEXT," + KEY_SUBITEM_URL+ " TEXT," //
					+ KEY_SUBITEM_TOPIC_PK+ " INTEGER,"//
					+ KEY_SUBITEM_PK+ " INTEGER,"//
					+ "FOREIGN KEY (" + KEY_SUBITEM_TOPIC_PK + ") REFERENCES " + DB_TABLE_TOPIC_DATA + " ("
					+ KEY_TOPIC_PK + "));";

			String commandMain = "create table "
					+ DB_TABLE_MAIN_DATA //
					+ " ("+ KEY_MAIN_ID+ " integer primary key autoincrement, " //
					+ KEY_MAIN_TITLE + " TEXT,"+ KEY_MAIN_PIC_URL+ " TEXT," //
					+ KEY_MAIN_TOPIC_PK+ " INTEGER,"//
					+ KEY_MAIN_SUB_PK + " INTEGER,"+ KEY_MAIN_POSTER+ " TEXT, " //
					+ KEY_MAIN_CONTENT + " TEXT, "+ KEY_MAIN_URL+ " TEXT, " //
					+ KEY_MAIN_EXTEND_DATA1 + " TEXT, "+ KEY_MAIN_EXTEND_DATA2+ " TEXT, " //
					+ KEY_MAIN_VALID + " INTEGER,"+ KEY_MAIN_PUBLISH_DATE+ " INTEGER, "//
					+ KEY_MAIN_FAVOR + " INTEGER NOT NULL DEFAULT 0, "//
					+ "FOREIGN KEY (" + KEY_MAIN_TOPIC_PK + ") REFERENCES " + DB_TABLE_TOPIC_DATA + " ("+ KEY_TOPIC_PK+ "),"//
					+ "FOREIGN KEY (" + KEY_MAIN_SUB_PK + ") REFERENCES " + DB_TABLE_SUBITEM_DATA + " ("+ KEY_SUBITEM_PK + "));";

			String commandContent = "create table " + DB_TABLE_CONTENT_DATA //
					+ " (" + KEY_CONTENT_ID + " integer primary key autoincrement, " //
					+ KEY_CONTENT_URL + " TEXT," + KEY_CONTENT_BODY + " TEXT, "//
					+ KEY_CONTENT_UPLOAD_DATE + " INTEGER,"//
					+ KEY_CONTENT_IMAGE_SET + " TEXT, "//
			        + KEY_CONTENT_ORIGIN + " TEXT, "
			        + KEY_CONTENT_FAVOR + " INTEGER NOT NULL DEFAULT 0 );";
			

			db.execSQL(commandTopic);
			db.execSQL(commandSubitem);
			db.execSQL(commandMain);
			db.execSQL(commandContent);
			
			
			//-------------db version 1003------------------
			createNewsTable(db);
		}
		
		private void createNewsTable(SQLiteDatabase db) {
			String commandNews = "create table " + DB_TABLE_NEWS_DATA//
					+ " (" + KEY_NEWS_ID + " integer primary key autoincrement, " //
					+ KEY_NEWS_TITLE + " TEXT," + KEY_NEWS_URL + " TEXT, "//
					+ KEY_NEWS_IMAGE_URL + " TEXT," + KEY_NEWS_DESCRIPTION + " TEXT, "//
					+ KEY_NEWS_UPLOAD_DATE + " INTEGER ); ";
			db.execSQL(commandNews);
		}

		private void addColumn(SQLiteDatabase db, String dbTable, String columnName, String columnDefinition) {
			db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + " " + columnDefinition);
		}

	}

}
