package com.comic.chhreader.clean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

import com.comic.chhreader.Loge;
import com.comic.chhreader.utils.FileOperation;
import com.comic.chhreader.utils.Utils;

public class CleanService extends Service {

	private static final String IMAGE_CACHE_FOLDER = Environment.getExternalStorageDirectory().getPath()
			+ "/ChhReader/Cache";
	private static final String IMAGE_CACHE_SUB_FOLDER = Environment.getExternalStorageDirectory().getPath()
			+ "/ChhReader/Cache/SUB";

	private Context mCtx;

	@Override
	public void onCreate() {
		mCtx = this;
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		new ImageCleanTask().execute();
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

		private class FileInfo {

			private String name;
			private String path;
			private long lastModified;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getPath() {
				return path;
			}

			public void setPath(String path) {
				this.path = path;
			}

			public long getLastModified() {
				return lastModified;
			}

			public void setLastModified(long lastModified) {
				this.lastModified = lastModified;
			}

		};

		class FileComparator implements Comparator<FileInfo> {
			@Override
			public int compare(FileInfo lhs, FileInfo rhs) {
				try {
					int leftId = Integer.valueOf(lhs.getName());
					int rightId = Integer.valueOf(rhs.getName());
					if (leftId > rightId) {
						return 1;
					} else {
						return -1;
					}
				} catch (NumberFormatException e) {
					return 1;
				}
			}
		}

		@Override
		protected void onPreExecute() {
			Loge.i("Start Clean Cache");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!Utils.isWifiAvailable(mCtx)) {
				return null;
			}

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
						if (gap > 3) {
							Loge.i("Gap Time = " + gap);
							item.delete();
						}
					}
				}
			}

			File foldersub = new File(IMAGE_CACHE_SUB_FOLDER);
			if (foldersub.exists() && foldersub.isDirectory()) {
				File[] subfiles = foldersub.listFiles();
				if (subfiles.length < 5) {
					return null;
				}
				List<FileInfo> filelist = new ArrayList<FileInfo>();
				for (File item : subfiles) {
					if (item.isDirectory()) {
						FileInfo info = new FileInfo();
						info.setName(item.getName());
						info.setPath(item.getPath());
						info.setLastModified(item.lastModified());
						filelist.add(info);
					}
				}
				if (filelist.size() > 0) {
					Collections.sort(filelist, new FileComparator());
					if (filelist.size() > 5) {
						Loge.i("Folder exsist, do delete catch");
						for (int i = 5; i < filelist.size(); i++) {
							File subitemfile = new File(filelist.get(i).getPath());
							FileOperation.deleteDirectory(subitemfile);
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
