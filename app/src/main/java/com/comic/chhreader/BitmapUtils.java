package com.comic.chhreader;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by zhangjunyi on 5/1/16.
 */
public class BitmapUtils {
    public static boolean saveJPEG(byte[] resource, String folder, String path) {
        if (resource == null || resource.length == 0 || path == null) {
            return false;
        }
        try {
            File folderFile = new File(folder);
            if (!folderFile.exists()) {
                folderFile.mkdir();
            }

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(resource);
            fos.flush();
            fos.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
