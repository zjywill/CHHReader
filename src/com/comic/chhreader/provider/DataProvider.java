package com.comic.chhreader.provider;

import com.comic.chhreader.Loge;

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

public class DataProvider extends ContentProvider {

	private static final String DB_NAME = "chhdatadb.db";
	private static final int DB_VERSION = 1001;
	public static final String DB_AUTHOR = "com.comic.chhreader";
	private DBHelper mOpenHelper;

	public static final Uri CONTENT_URI_MAIN_DATA = Uri.parse("content://com.comic.chhreader/main");

	// Name of table in the database
	private static final String DB_TABLE_MAIN_DATA = "main";

	// main table data keys
	public static final String KEY_MAIN_ID = "_id";
	public static final String KEY_MAIN_TITLE = "title";
	public static final String KEY_MAIN_PIC_URL = "picture";
	public static final String KEY_MAIN_SUB_TITLE = "subtitle";
	public static final String KEY_MAIN_CATEGORY = "category";
	public static final String KEY_MAIN_SHORTCUT = "shortcut";
	public static final String KEY_MAIN_POSTER = "poster";
	public static final String KEY_MAIN_CONTENT = "content";
	public static final String KEY_MAIN_URL = "url";
	public static final String KEY_MAIN_EXTEND_DATA1 = "extend1";
	public static final String KEY_MAIN_EXTEND_DATA2 = "extend2";
	public static final String KEY_MAIN_PUBLISH_DATE = "date";

	@Override
	public boolean onCreate() {
		Loge.d("Create DataBase CHH Data");
		Context context = getContext();
		mOpenHelper = new DBHelper(context);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(getTable(uri));

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
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

		long rowID = 0;
		try {
			rowID = db.insert(table_name, KEY_MAIN_CONTENT, values);
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
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		int count = 0;
		try {
			count = db.update(table_name, values, selection, selectionArgs);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/vnd.comic.chhreader";
	}

	private String getTable(Uri uri) {
		String sUri = uri.toString();
		if (sUri.equals(CONTENT_URI_MAIN_DATA.toString())) {
			return DB_TABLE_MAIN_DATA;
		}
		return "";
	}

	public class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String commandMain = "create table " + DB_TABLE_MAIN_DATA + " (" + KEY_MAIN_ID + " integer primary key autoincrement, " + KEY_MAIN_TITLE + " TEXT," + KEY_MAIN_PIC_URL + " TEXT," + KEY_MAIN_SUB_TITLE + " TEXT," + KEY_MAIN_CATEGORY + " TEXT," + KEY_MAIN_SHORTCUT + " TEXT, " + KEY_MAIN_POSTER + " TEXT, " + KEY_MAIN_CONTENT + " TEXT, " + KEY_MAIN_URL + " TEXT, " + KEY_MAIN_EXTEND_DATA1 + " TEXT, " + KEY_MAIN_EXTEND_DATA2 + " TEXT, " + KEY_MAIN_PUBLISH_DATE + " INTEGER );";
			db.execSQL(commandMain);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_MAIN_DATA);
		}

	}

}
