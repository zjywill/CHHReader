package com.comic.chhreader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtils {

	public static final String PREFERENCES_NAME = "chhreader";

	public static void saveUpdateTime(Context context, long time) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putLong("updateTime", time);
		editor.commit();
	}

	public static long getUpdateTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		return pref.getLong("updateTime", 0);
	}

	public static void saveNoImageMode(Context context, boolean check) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putBoolean("noimagemode", check);
		editor.commit();
	}

	public static boolean getNoImageMode(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		return pref.getBoolean("noimagemode", true);
	}

}
