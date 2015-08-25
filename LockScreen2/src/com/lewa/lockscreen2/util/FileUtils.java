
package com.lewa.lockscreen2.util;


import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lewa on 2/10/15.
 */
public class FileUtils {

    public static void checkSD() {

    }

    public static void initWallpaperFile() {

        File wallpaperDir = new File(Constant.WALLPAPER_PATH);
        if (!wallpaperDir.exists()) {
            wallpaperDir.mkdirs();
        }

        File iconDir = new File(Constant.RECOMMENDAPP_ICON_PATH);
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }

        File appDir = new File(Constant.RECOMMENDAPP_APP);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
    }

    public static boolean isExists(String path){
        File file = new File(path);
        return file.exists();
    }

    public static String[] getWallpaperPath() {
        String path[] = null;
        File fileDir = new File(Constant.WALLPAPER_PATH);
        if (fileDir != null && fileDir.isDirectory()) {
            File files[] = fileDir.listFiles();
            path = new String[files.length];
            for (int i = 0, L = files.length; i < L; i++) {
                path[i] = files[i].getAbsolutePath();
            }
        }

        return path;
    }

    public static void deleteWallpaper() {

        File fileDir = new File(Constant.WALLPAPER_PATH);
        if (fileDir != null && fileDir.isDirectory()) {
            File files[] = fileDir.listFiles();
            int length = files.length;
            if (length <= Constant.MAX_WALLPAPER_COUNT) {
                return;
            }

            List<File> list = Arrays.asList(files);
            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File file, File file2) {
                    if (file2.lastModified() - file.lastModified() > 0){
                        return 1;
                    } else if (file2.lastModified() - file.lastModified() == 0){
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });

            for (int i = Constant.MAX_WALLPAPER_COUNT; i < length; i++) {
                list.get(i).delete();
            }
        }
    }

    public static int getWallpaperCount() {
        int count = 0;
        File fileDir = new File(Constant.WALLPAPER_PATH);
        if (fileDir != null && fileDir.isDirectory()) {
            File files[] = fileDir.listFiles();
            count = files.length;
        }
        return count;
    }

    public static boolean installApk(String filePath) {

        LogUtil.d("installApk()--------> filePath:" + filePath);
        String[] args = {"pm", "install", "-r", filePath};
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (Exception e) {
            LogUtil.d("error:" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                LogUtil.d("error:" + e.getMessage());
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (result != null && (result.endsWith("Success") || result.endsWith("Success\n"))) {
            LogUtil.d("installApk --------------> success");
            deleteFile(filePath);
            return true;
        }

        LogUtil.d("installApk --------------> fail");
        // deleteFile(filePath);
        return false;
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public static boolean isExistsApk(Context context, String packageName, String className) {
        try {
            context.getPackageManager().getActivityInfo(
                    new ComponentName(packageName, className), 0);
            return true;
        } catch (Exception e) {
            LogUtil.e("isExistsApk() --------------> is not exist className:" + className);
            return false;
        }
    }

    public static boolean isExistsApk(Context context, String packageName) {
        try {
            int version = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
            LogUtil.e("isExistsApk() --------------> is exist packageName:" + packageName + ", " + version);
            return true;
        } catch (Exception e) {
            LogUtil.e("isExistsApk() --------------> is not exist packageName:" + packageName);
            return false;
        }
    }
}

