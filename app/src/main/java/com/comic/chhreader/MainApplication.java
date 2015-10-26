package com.comic.chhreader;

import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

import com.comic.chhreader.imageloader.ImageCacheManager;
import com.comic.chhreader.imageloader.ImageCacheManager.CacheType;
import com.comic.chhreader.imageloader.RequestManager;

public class MainApplication extends Application {

    private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 120;
    private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static int DISK_IMAGECACHE_QUALITY = 20; //PNG is lossless so quality is ignored but must be provided

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Intialize the request manager and the image cache
     */
    private void init() {
        RequestManager.init(this);
        createImageCache();
    }

    /**
     * Create the image cache. Uses Memory Cache by default. Change to Disk for
     * a Disk based LRU implementation.
     */
    private void createImageCache() {
        ImageCacheManager.getInstance().init(this, this.getPackageCodePath(), DISK_IMAGECACHE_SIZE,
                DISK_IMAGECACHE_COMPRESS_FORMAT, DISK_IMAGECACHE_QUALITY, CacheType.DISK);
    }
}
