/*
 * Copyright (C) ${year} The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comic.chhreader.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;

import com.comic.chhreader.Loge;
import com.comic.chhreader.image.PhotoDecodeRunnable.TaskRunnableDecodeMethods;

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
	// Sets the image cache folder in external storage
	private static final String IMAGE_CACHE_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/ChhReader/Cache";

	// Sets the size for each read action (bytes)
	private static final int READ_SIZE = 1024;

	// Sets a tag for this class
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "PhotoDownloadRunnable";

	// Constants for indicating the state of the download
	static final int HTTP_STATE_FAILED = -1;
	static final int HTTP_STATE_STARTED = 0;
	static final int HTTP_STATE_COMPLETED = 1;

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
		 * Returns the current contents of the download buffer
		 * 
		 * @return The byte array downloaded from the URL in the last read
		 */
		byte[] getByteBuffer();

		/**
		 * Sets the current contents of the download buffer
		 * 
		 * @param buffer
		 *            The bytes that were just read
		 */
		void setByteBuffer(byte[] buffer);

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
		 * Get as loading value
		 * 
		 * @return The as loading value.
		 */
		boolean getLoadNetImage();
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

		/*
		 * Gets the image cache buffer object from the PhotoTask instance. This
		 * makes the to both PhotoDownloadRunnable and PhotoTask.
		 */
		byte[] byteBuffer = mPhotoTask.getByteBuffer();

		// gen photo name in cache
		String[] photoFileNameArray = mPhotoTask.getImageURL().toString().split("/");
		String photoFileName = null;
		if (photoFileNameArray.length > 2) {
			photoFileName = photoFileNameArray[photoFileNameArray.length - 2] + photoFileNameArray[photoFileNameArray.length - 1];
		} else {
			photoFileName = photoFileNameArray[photoFileNameArray.length - 1];
		}

		Loge.d("photoFileName = " + photoFileName);
		photoFileName = photoFileName + ".chhreader";

		synchronized (this) {
			if (null == byteBuffer) {
				try {
					// get photo from local storage cache
					File folder = new File(IMAGE_CACHE_FOLDER);
					if (!folder.exists()) {
						Loge.w("Folder not exsist, create new folder");
						folder.mkdirs();
					}

					String path = IMAGE_CACHE_FOLDER + "/" + photoFileName;
					Loge.i("Cache photo path = " + path);
					File photoFile = new File(path);
					if (photoFile.exists()) {
						Loge.i("Photo file exsist, get photo from local cache");
						FileInputStream fis = new FileInputStream(photoFile);
						if (fis.available() != 0) {
							byteBuffer = new byte[fis.available()];
							int result = fis.read(byteBuffer);
							if (result == 0) {
								Loge.i("read error = " + result);
								byteBuffer = null;
								photoFile.delete();
							}
						} else {
							photoFile.delete();
						}
						fis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (byteBuffer == null) {
			Loge.d("no local data, load fail");
		}

		/*
		 * A try block that downloads a Picasa image from a URL. The URL value
		 * is in the field PhotoTask.mImageURL
		 */
		// Tries to download the picture from Picasa
		try {
			if (byteBuffer == null) {

				Loge.d("LoadNetImage: " + mPhotoTask.getLoadNetImage());
				if (!mPhotoTask.getLoadNetImage()) {
					// mPhotoTask.handleDownloadState(HTTP_STATE_FAILED);
					return;
				}

				try {
					// Before continuing, checks to see that the Thread hasn't
					// been
					// interrupted
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					// If there's no cache buffer for this image
					/*
					 * Calls the PhotoTask implementation of {@link
					 * #handleDownloadState} to set the state of the download
					 */
					mPhotoTask.handleDownloadState(HTTP_STATE_STARTED);

					// Defines a handle for the byte download stream
					InputStream byteStream = null;

					// Downloads the image and catches IO errors

					// Opens an HTTP connection to the image's URL
					HttpURLConnection httpConn;
					httpConn = (HttpURLConnection) mPhotoTask.getImageURL().openConnection();

					// Sets the user agent to report to the server
					httpConn.setRequestProperty("User-Agent", PhotoConstants.USER_AGENT);
					httpConn.setRequestMethod("GET");
					httpConn.setReadTimeout(5000);
					httpConn.setDoInput(true);

					// Before continuing, checks to see that the Thread
					// hasn't been interrupted
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					// Gets the input stream containing the image
					byteStream = httpConn.getInputStream();

					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					String path = IMAGE_CACHE_FOLDER + "/" + photoFileName;
					File tempPhoto = new File(path);
					OutputStream outputStream = new FileOutputStream(tempPhoto);

					int contentSize = httpConn.getContentLength();

					byte buffer[] = new byte[READ_SIZE];
					int bufferLength = 0;
					while ((bufferLength = byteStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, bufferLength);
					}

					if (byteStream != null) {
						byteStream.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}

					if (tempPhoto.exists()) {
						Loge.i("Photo file exsist, get photo from local cache");
						FileInputStream fis = new FileInputStream(tempPhoto);
						if (fis.available() < contentSize || fis.available() == 0) {
							Loge.i("Photo file not complete, delete local file");
							fis.close();
							tempPhoto.delete();
						}
						byteBuffer = new byte[fis.available()];
						int result = fis.read(byteBuffer);
						fis.close();
						if (result == 0) {
							Loge.i("read error = " + result);
							byteBuffer = null;
							tempPhoto.delete();
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (byteBuffer == null) {
				mPhotoTask.handleDownloadState(HTTP_STATE_FAILED);
				return;
			}

			/*
			 * Stores the downloaded bytes in the byte buffer in the PhotoTask
			 * instance.
			 */
			mPhotoTask.setByteBuffer(byteBuffer);

			/*
			 * Sets the status message in the PhotoTask instance. This sets the
			 * ImageView background to indicate that the image is being decoded.
			 */
			Loge.d("download HTTP_STATE_COMPLETED");
			mPhotoTask.handleDownloadState(HTTP_STATE_COMPLETED);

			// Catches exceptions thrown in response to a queued interrupt
		} catch (InterruptedException e1) {

			// Does nothing

			// In all cases, handle the results
		} finally {

			// If the byteBuffer is null, reports that the download failed.
			if (null == byteBuffer) {
				mPhotoTask.handleDownloadState(HTTP_STATE_FAILED);
			}

			/*
			 * The implementation of setHTTPDownloadThread() in PhotoTask calls
			 * PhotoTask.setCurrentThread(), which then locks on the static
			 * ThreadPool object and returns the current thread. Locking keeps
			 * all references to Thread objects the same until the reference to
			 * the current Thread is deleted.
			 */

			// Sets the reference to the current Thread to null, releasing its
			// storage
			mPhotoTask.setDownloadThread(null);

			// Clears the Thread's interrupt flag
			Thread.interrupted();
		}
	}
}
