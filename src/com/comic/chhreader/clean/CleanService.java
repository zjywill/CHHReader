package com.comic.chhreader.clean;

import java.io.File;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

import com.comic.chhreader.Loge;

public class CleanService extends Service {

	private static final String IMAGE_CACHE_FOLDER = Environment.getExternalStorageDirectory().getPath()
			+ "/ChhReader/Cache";

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		new ImageCleanTask().execute();
		super.onStart(intent, startId);
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	class ImageCleanTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			Loge.i("Start Clean Cache");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			File folder = new File(IMAGE_CACHE_FOLDER);
			Date date = new Date();
			long now = date.getTime();

			if (folder.exists()) {
				Loge.i("Folder exsist, cout files");
				File[] files = folder.listFiles();
				for (File item : files) {
					if (item.isFile()) {
						long lastTime = item.lastModified();
						long gap = ((now - lastTime) / 1000) / 604800;
						if (gap > 7) {
							Loge.i("Gap Time = " + gap);
							item.delete();
						}
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Loge.i("Clean Cache Finished");
			stopSelf();
			super.onPostExecute(result);
		}

	}

}
