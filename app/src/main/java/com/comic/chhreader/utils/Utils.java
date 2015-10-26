package com.comic.chhreader.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

public class Utils {
	static public boolean isLocationProvideOn(Context ctx) {
		final LocationManager locationMgr = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enable = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network_enable = locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		return gps_enable || network_enable;
	};

	static public boolean isNetworkAvailable(Context ctx) {
		final ConnectivityManager connectivity = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			final NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) {
				return true;
			}
		}
		return false;
	}

	static public boolean isWifiAvailable(Context ctx) {
		final ConnectivityManager connectivity = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ninfo = connectivity.getActiveNetworkInfo();
		if (ninfo != null && ninfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	static public boolean isMobileNetworkAvailable(Context ctx) {
		final ConnectivityManager connectivity = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ninfo = connectivity.getActiveNetworkInfo();
		if (ninfo != null && ninfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

	static public int dipToPx(Context context, int dimen) {
		final DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return ((int) (dimen * dm.density));
	}
}
