package com.comic.chhreader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

import com.comic.chhreader.Loge;

public class FileOperation {

	FileOperation(Context context) {

	}

	public static String copyFileToSDcard(Context context, File originFile) {
		String pkgName = context.getPackageName();
		String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + pkgName + "/Cache";
		String fileName = originFile.getName();
		File folder = new File(filePath);
		if (!folder.exists()) {
			folder.mkdirs();
		} else {
			if (deleteDirectory(folder))
				folder.mkdirs();
		}
		File targetFile = new File(filePath + "/" + fileName);
		if (!targetFile.exists()) {
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				Loge.w("Create File Failed", e);
				return null;
			}
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(originFile);
		} catch (FileNotFoundException e) {
			Loge.w("OriginFile doesn't exsist", e);
			return null;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
			int n = 0;
			int blockSize = 1000;
			byte[] byteBuffer = new byte[blockSize];
			while ((n = fis.read(byteBuffer)) != -1) {
				fos.write(byteBuffer, 0, n);
			}
		} catch (FileNotFoundException e) {
			Loge.w("TargetFile doesn't exsist", e);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		try {
			fis.close();
			fos.close();
		} catch (IOException e) {
			Loge.w("File close failed", e);
			return null;
		}
		return filePath + "/" + fileName;
	}

	public static boolean deleteDirectory(File folder) {
		boolean result = true;
		if (!folder.exists() || !folder.isDirectory()) {
			return false;
		}
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				result = deleteFile(files[i]);
				if (!result)
					break;
			} else {
				result = deleteDirectory(files[i]);
				if (!result)
					break;
			}
		}
		if (result)
			return folder.delete();
		else
			return false;
	}

	public static boolean deleteFile(File file) {
		boolean result = false;
		if (file.isFile() && file.exists()) {
			file.delete();
			result = true;
		}
		return result;
	}
}
