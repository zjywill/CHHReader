package com.comic.chhreader.image;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;

import com.comic.chhreader.image.PhotoDownloadRunnable.TaskRunnableDownloadMethods;

/**
 * This class manages PhotoDownloadRunnable and PhotoDownloadRunnable objects.
 * It does't perform the download or decode; instead, it manages persistent
 * storage for the tasks that do the work. It does this by implementing the
 * interfaces that the download and decode classes define, and then passing
 * itself as an argument to the constructor of a download or decode object. In
 * effect, this allows PhotoTask to start on a Thread, run a download in a
 * delegate object, then run a decode, and then start over again. This class can
 * be pooled and reused as necessary.
 */
public class PhotoTask implements TaskRunnableDownloadMethods {

	/*
	 * Creates a weak reference to the ImageView that this Task will populate.
	 * The weak reference prevents memory leaks and crashes, because it
	 * automatically tracks the "state" of the variable it backs. If the
	 * reference becomes invalid, the weak reference is garbage- collected. This
	 * technique is important for referring to objects that are part of a
	 * component lifecycle. Using a hard reference may cause memory leaks as the
	 * value continues to change; even worse, it can cause crashes if the
	 * underlying component is destroyed. Using a weak reference to a View
	 * ensures that the reference is more transitory in nature.
	 */
	private WeakReference<RemoteImageLoaderListener> mImageListenrWeakRef;

	// The image's URL
	private URL mImageURL;

	/*
	 * Field containing the Thread this task is running on.
	 */
	Thread mThreadThis;

	/*
	 * Fields containing references to the two runnable objects that handle
	 * downloading and decoding of the image.
	 */
	private Runnable mDownloadRunnable;

	// The decoded image
	private Bitmap mDecodedImage;

	// The Thread on which this task is currently running.
	private Thread mCurrentThread;

	/*
	 * An object that contains the ThreadPool singleton.
	 */
	private static PhotoManager sPhotoManager;

	private String mImageDownloadFolder;

	private String mImagePath;

	/**
	 * Creates an PhotoTask containing a download object and a decoder object.
	 */
	PhotoTask() {
		// Create the runnables
		mDownloadRunnable = new PhotoDownloadRunnable(this);
		sPhotoManager = PhotoManager.getInstance();
	}

	/**
	 * Initializes the Task
	 * 
	 * @param photoManager
	 *            A ThreadPool object
	 * @param photoView
	 *            An ImageView instance that shows the downloaded image
	 * @param cacheFlag
	 *            Whether caching is enabled
	 */
	void initializeDownloaderTask(PhotoManager photoManager, String imageurl, String downloadPath,
			RemoteImageLoaderListener imagelistener) {
		// Sets this object's ThreadPool field to be the input argument
		sPhotoManager = photoManager;

		// Gets the URL for the View
		try {
			mImageURL = new URL(imageurl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String photoFileName = PhotoUtils.getPhotoName(getImageURL().toString());
		if (downloadPath == null) {
			downloadPath = PhotoManager.IMAGE_CACHE_FOLDER;
		}
		setImageDownloadFolder(downloadPath);
		String photoPath = downloadPath + "/" + photoFileName;
		setImagePath(photoPath);

		// Instantiates the weak reference to the incoming view
		mImageListenrWeakRef = new WeakReference<RemoteImageLoaderListener>(imagelistener);

		// Sets the cache flag to the input argument
	}

	/**
	 * Recycles an PhotoTask object before it's put back into the pool. One
	 * reason to do this is to avoid memory leaks.
	 */
	void recycle() {

		// Deletes the weak reference to the imageView
		if (null != mImageListenrWeakRef) {
			mImageListenrWeakRef.clear();
			mImageListenrWeakRef = null;
		}

		// Releases references to the byte buffer and the BitMap
		mDecodedImage = null;
	}

	public RemoteImageLoaderListener getImageListener() {
		if (null != mImageListenrWeakRef) {
			return mImageListenrWeakRef.get();
		}
		return null;
	}

	// Implements PhotoDownloadRunnable.getImageURL. Returns the global Image
	// URL.
	@Override
	public URL getImageURL() {
		return mImageURL;
	}

	// Delegates handling the current state of the task to the PhotoManager
	// object
	void handleState(int state) {
		sPhotoManager.handleState(this, state);
	}

	// Returns the image that PhotoDecodeRunnable decoded.
	Bitmap getImage() {
		return mDecodedImage;
	}

	// Returns the instance that downloaded the image
	Runnable getHTTPDownloadRunnable() {
		return mDownloadRunnable;
	}

	/*
	 * Returns the Thread that this Task is running on. The method must first
	 * get a lock on a static field, in this case the ThreadPool singleton. The
	 * lock is needed because the Thread object reference is stored in the
	 * Thread object itself, and that object can be changed by processes outside
	 * of this app.
	 */
	public Thread getCurrentThread() {
		synchronized (sPhotoManager) {
			return mCurrentThread;
		}
	}

	/*
	 * Sets the identifier for the current Thread. This must be a synchronized
	 * operation; see the notes for getCurrentThread()
	 */
	public void setCurrentThread(Thread thread) {
		synchronized (sPhotoManager) {
			mCurrentThread = thread;
		}
	}

	// Implements ImageCoderRunnable.setImage(). Sets the Bitmap for the current
	// image.
	@Override
	public void setImage(Bitmap decodedImage) {
		mDecodedImage = decodedImage;
	}

	// Implements PhotoDownloadRunnable.setHTTPDownloadThread(). Calls
	// setCurrentThread().
	@Override
	public void setDownloadThread(Thread currentThread) {
		setCurrentThread(currentThread);
	}

	/*
	 * Implements PhotoDownloadRunnable.handleHTTPState(). Passes the download
	 * state to the ThreadPool object.
	 */

	@Override
	public void handleDownloadState(int state) {
		int outState;

		// Converts the download state to the overall state
		switch (state) {
			case PhotoDownloadRunnable.HTTP_STATE_COMPLETED:
				outState = PhotoManager.DOWNLOAD_COMPLETE;
				break;
			case PhotoDownloadRunnable.HTTP_STATE_FAILED:
				outState = PhotoManager.DOWNLOAD_FAILED;
				break;
			case PhotoDownloadRunnable.CACHE_LOADED:
				outState = PhotoManager.CACHE_LOADED_COMPLETE;
				break;
			default:
				outState = PhotoManager.DOWNLOAD_STARTED;
				break;
		}
		// Passes the state to the ThreadPool object.
		handleState(outState);
	}

	@Override
	public void setImagePath(String imagepath) {
		mImagePath = imagepath;
	}

	@Override
	public String getImagePath() {
		return mImagePath;
	}

	public String getImageDownloadFolder() {
		return mImageDownloadFolder;
	}

	public void setImageDownloadFolder(String mImageDownloadFolder) {
		this.mImageDownloadFolder = mImageDownloadFolder;
	}
}
