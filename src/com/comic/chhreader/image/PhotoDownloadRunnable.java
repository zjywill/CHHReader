package com.comic.chhreader.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

import com.comic.chhreader.Loge;

/**
 * This task downloads bytes from a resource addressed by a URL. When the task
 * has finished, it calls handleState to report its results.
 * 
 * Objects of this class are instantiated and managed by instances of PhotoTask,
 * which implements the methods of {@link TaskRunnableDecodeMethods}. PhotoTask
 * objects call {@link #PhotoDownloadRunnable(TaskRunnableDownloadMethods)
 * PhotoDownloadRunnable()} with themselves as the argument. In effect, an
 * PhotoTask object and a PhotoDownloadRunnable object communicate through the
 * fields of the PhotoTask.
 */
class PhotoDownloadRunnable implements Runnable {
	private static final String PNG_APPENDIX = ".png";

	// Constants for indicating the state of the download
	static final int HTTP_STATE_FAILED = -1;
	static final int HTTP_STATE_STARTED = 0;
	static final int HTTP_STATE_COMPLETED = 1;
	static final int CACHE_LOADED = 2;

	// Defines a field that contains the calling object of type PhotoTask.
	final TaskRunnableDownloadMethods mPhotoTask;

	/**
	 * 
	 * An interface that defines methods that PhotoTask implements. An instance
	 * of PhotoTask passes itself to an PhotoDownloadRunnable instance through
	 * the PhotoDownloadRunnable constructor, after which the two instances can
	 * access each other's variables.
	 */
	interface TaskRunnableDownloadMethods {

		/**
		 * Sets the Thread that this instance is running on
		 * 
		 * @param currentThread
		 *            the current Thread
		 */
		void setDownloadThread(Thread currentThread);

		/**
		 * Defines the actions for each state of the PhotoTask instance.
		 * 
		 * @param state
		 *            The current state of the task
		 */
		void handleDownloadState(int state);

		/**
		 * Gets the URL for the image being downloaded
		 * 
		 * @return The image URL
		 */
		URL getImageURL();

		/**
		 * Gets the Image path for the image downloaded
		 * 
		 * @return The image Path
		 */
		String getImagePath();

		/**
		 * Sets the Image path for the image downloaded
		 * 
		 */
		void setImagePath(String imagepath);

		/**
		 * Sets the Image folder name for the image downloaded
		 * 
		 */
		void setImageDownloadFolder(String mImageDownloadFolder);

		/**
		 * Sets the Bitmap for the ImageView being displayed.
		 * 
		 * @param image
		 */
		void setImage(Bitmap image);
	}

	/**
	 * This constructor creates an instance of PhotoDownloadRunnable and stores
	 * in it a reference to the PhotoTask instance that instantiated it.
	 * 
	 * @param photoTask
	 *            The PhotoTask, which implements TaskRunnableDecodeMethods
	 */
	PhotoDownloadRunnable(TaskRunnableDownloadMethods photoTask) {
		mPhotoTask = photoTask;
	}

	/*
	 * Defines this object's task, which is a set of instructions designed to be
	 * run on a Thread.
	 */
	@Override
	public void run() {

		/*
		 * Stores the current Thread in the the PhotoTask instance, so that the
		 * instance can interrupt the Thread.
		 */
		mPhotoTask.setDownloadThread(Thread.currentThread());

		// Moves the current Thread into the background
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		Bitmap targetBitmap = null;

		// gen photo name in cache
		String photoPath = mPhotoTask.getImagePath();

		targetBitmap = loadBitmap(photoPath);

		if (targetBitmap != null) {
			mPhotoTask.setImage(targetBitmap);
			mPhotoTask.handleDownloadState(CACHE_LOADED);
			return;
		}

		mPhotoTask.handleDownloadState(HTTP_STATE_STARTED);

		try {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			InputStream stream = null;
			try {
				stream = mPhotoTask.getImageURL().openStream();
				targetBitmap = BitmapFactory.decodeStream(stream);

				if (targetBitmap != null) {
					if (photoPath.toLowerCase().endsWith(PNG_APPENDIX)) {
						saveBitmap(targetBitmap, photoPath, Bitmap.CompressFormat.PNG);
					} else {
						saveBitmap(targetBitmap, photoPath, Bitmap.CompressFormat.JPEG);
					}
					mPhotoTask.setImage(targetBitmap);
					mPhotoTask.handleDownloadState(HTTP_STATE_COMPLETED);
				}
			} catch (Exception e) {
				Loge.e("image download failed");
			} finally {
				Loge.i("image download success");
				mPhotoTask.setDownloadThread(null);
				Thread.interrupted();
			}
		} catch (InterruptedException e1) {
		}
	}

	private Bitmap loadBitmap(String aFileName) {
		Bitmap bitmap = null;
		File file = new File(aFileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			bitmap = BitmapFactory.decodeStream(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Loge.e("FileNotFoundException");
		} catch (IOException e) {
			Loge.e("IOException");
		} catch (OutOfMemoryError e) {
			Loge.e("OutOfMemoryError");
		}
		return bitmap;
	}

	private boolean saveBitmap(Bitmap bmp, String path, CompressFormat format) {
		if (bmp == null || path == null) {
			return false;
		}
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bmp.compress(format, 100, baos);
			fos.write(baos.toByteArray());
			baos.flush();
			fos.flush();
			baos.close();
			fos.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
