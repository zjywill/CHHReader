package com.comic.chhreader.image;

import java.net.URL;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class PhotoManager {

	// Sets the image cache folder in external storage, default image downloading path
	public static final String IMAGE_CACHE_FOLDER = Environment.getExternalStorageDirectory().getPath()
			+ "/Android/data/com.comic.chhreader/Images/ImageLoader";
	/*
	 * Status indicators
	 */
	static final int DOWNLOAD_FAILED = -1;
	static final int DOWNLOAD_STARTED = 1;
	static final int DOWNLOAD_COMPLETE = 2;
	static final int CACHE_LOADED_COMPLETE = 3;

	// Sets the amount of time an idle thread will wait for a task before
	// terminating
	private static final int KEEP_ALIVE_TIME = 1;

	// Sets the Time Unit to seconds
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

	// Sets the initial threadpool size to 1,set to 1 to prevent racing local
	// image file, prevent multiple access
	private static final int CORE_POOL_SIZE = 1;

	// Sets the maximum threadpool size to 1,set to 1 to prevent racing local
	// image file, prevent multiple access
	private static final int MAXIMUM_POOL_SIZE = 1;

	// A queue of Runnables for the image download pool
	private final BlockingQueue<Runnable> mDownloadWorkQueue;

	// A queue of PhotoManager tasks. Tasks are handed to a ThreadPool.
	private final Queue<PhotoTask> mPhotoTaskWorkQueue;

	// A managed pool of background download threads
	private final ThreadPoolExecutor mDownloadThreadPool;

	// An object that manages Messages in a Thread
	private Handler mHandler;

	// A single instance of PhotoManager, used to implement the singleton
	// pattern
	private static PhotoManager sInstance = null;

	// A static block that sets class fields
	static {

		// The time unit for "keep alive" is in seconds
		KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

		// Creates a single static instance of PhotoManager
		sInstance = new PhotoManager();
	}

	/**
	 * Constructs the work queues and thread pools used to download and decode
	 * images.
	 */
	private PhotoManager() {

		/*
		 * Creates a work queue for the pool of Thread objects used for
		 * downloading, using a linked list queue that blocks when the queue is
		 * empty.
		 */
		mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();

		/*
		 * Creates a work queue for the set of of task objects that control
		 * downloading and decoding, using a linked list queue that blocks when
		 * the queue is empty.
		 */
		mPhotoTaskWorkQueue = new LinkedBlockingQueue<PhotoTask>();

		/*
		 * Creates a new pool of Thread objects for the download work queue
		 */
		mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
				KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);
		/*
		 * Instantiates a new anonymous Handler object and defines its
		 * handleMessage() method. The Handler *must* run on the UI thread,
		 * because it moves photo Bitmaps from the PhotoTask object to the View
		 * object. To force the Handler to run on the UI thread, it's defined as
		 * part of the PhotoManager constructor. The constructor is invoked when
		 * the class is first referenced, and that happens when the View invokes
		 * startDownload. Since the View runs on the UI Thread, so does the
		 * constructor and the Handler.
		 */
		mHandler = new Handler(Looper.getMainLooper()) {

			/*
			 * handleMessage() defines the operations to perform when the
			 * Handler receives a new Message to process.
			 */
			@Override
			public void handleMessage(Message inputMessage) {

				// Gets the image task from the incoming Message object.
				PhotoTask photoTask = (PhotoTask) inputMessage.obj;

				if (photoTask == null) {
					return;
				}

				// Sets an PhotoView that's a weak reference to the
				// input ImageView
				RemoteImageLoaderListener localViewListener = photoTask.getImageListener();

				// If this input view isn't null
				if (localViewListener != null) {
					/*
					 * Chooses the action to take, based on the incoming message
					 */
					switch (inputMessage.what) {
					// If the download has started, sets background color to
					// dark green
						case DOWNLOAD_STARTED:
							localViewListener.onDownloadStart(photoTask.getImageURL().toString());
							break;
						case CACHE_LOADED_COMPLETE:
							localViewListener.onCacheLoaded(photoTask.getImagePath(), photoTask.getImage());
							recycleTask(photoTask);
							break;
						case DOWNLOAD_COMPLETE:
							// Sets background color to golden yellow
							localViewListener.onDownloadComplete(photoTask.getImageURL().toString(),
									photoTask.getImage());
							recycleTask(photoTask);
							break;
						case DOWNLOAD_FAILED:
							localViewListener.onDownloadFailed();
							recycleTask(photoTask);
							break;
						default:
							// Otherwise, calls the super method
							super.handleMessage(inputMessage);
					}
				}
			}
		};
	}

	/**
	 * Returns the PhotoManager object
	 * 
	 * @return The global PhotoManager object
	 */
	public static PhotoManager getInstance() {

		return sInstance;
	}

	/**
	 * Handles state messages for a particular task object
	 * 
	 * @param photoTask
	 *            A task object
	 * @param state
	 *            The state of the task
	 */
	public void handleState(PhotoTask photoTask, int state) {
		mHandler.obtainMessage(state, photoTask).sendToTarget();
	}

	/**
	 * Stops a download Thread and removes it from the threadpool
	 * 
	 * @param downloaderTask
	 *            The download task associated with the Thread
	 * @param pictureURL
	 *            The URL being downloaded
	 */
	static public void removeDownload(PhotoTask downloaderTask, URL pictureURL) {
		// If the Thread object still exists and the download matches the
		// specified URL
		if (downloaderTask != null && downloaderTask.getImageURL().equals(pictureURL)) {
			/*
			 * Locks on this class to ensure that other processes aren't
			 * mutating Threads.
			 */
			synchronized (sInstance) {

				// Gets the Thread that the downloader task is running on
				Thread thread = downloaderTask.getCurrentThread();

				// If the Thread exists, posts an interrupt to it
				if (null != thread)
					thread.interrupt();
			}
			/*
			 * Removes the download Runnable from the ThreadPool. This opens a
			 * Thread in the ThreadPool's work queue, allowing a task in the
			 * queue to start.
			 */
			sInstance.mDownloadThreadPool.remove(downloaderTask.getHTTPDownloadRunnable());
		}
	}

	/**
	 * Starts an image download and decode
	 * 
	 * @param imageView
	 *            The ImageView that will get the resulting Bitmap
	 * @param cacheFlag
	 *            Determines if caching should be used
	 * @return The task instance that will handle the work
	 */
	static public PhotoTask startDownload(String imageurl, String downloadPath,
			RemoteImageLoaderListener imagelistener) {

		/*
		 * Gets a task from the pool of tasks, returning null if the pool is
		 * empty
		 */
		PhotoTask downloadTask = sInstance.mPhotoTaskWorkQueue.poll();

		// If the queue was empty, create a new task instead.
		if (null == downloadTask) {
			downloadTask = new PhotoTask();
		}

		// Initializes the task
		downloadTask.initializeDownloaderTask(PhotoManager.sInstance, imageurl, downloadPath, imagelistener);

		sInstance.mDownloadThreadPool.execute(downloadTask.getHTTPDownloadRunnable());

		// Returns a task object, either newly-created or one from the task pool
		return downloadTask;
	}

	/**
	 * Recycles tasks by calling their internal recycle() method and then
	 * putting them back into the task queue.
	 * 
	 * @param downloadTask
	 *            The task to recycle
	 */
	void recycleTask(PhotoTask downloadTask) {

		// Frees up memory in the task
		downloadTask.recycle();

		// Puts the task object back into the queue for re-use.
		mPhotoTaskWorkQueue.offer(downloadTask);
	}
}
