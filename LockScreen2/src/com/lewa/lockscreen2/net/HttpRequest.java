
package com.lewa.lockscreen2.net;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.lewa.lockscreen2.util.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lewa on 2/10/15.
 */
public class HttpRequest {

    private Context mContext;
    private Context mLockScreenContext;
    private JsonParse mJsonParse;
    private DbUtil mDbUtil;
    private BitmapCache mBitmapCache;
    private int mDownloadType;
    private UpdateImageThread mUpdateImageThread;
    private String mUrl;
    private int mWidth;
    private int mHeight;

    public HttpRequest(Context context, Context lockScreenContext) {
        this.mContext = context;
        mLockScreenContext = lockScreenContext;
        this.mJsonParse = new JsonParse();
        mDbUtil = new DbUtil(mContext);
        mBitmapCache = BitmapCache.getInstance(context);
        mUpdateImageThread = new UpdateImageThread();
    }

    public void updateImage(int downloadType, final int w, final int h, final String url) {
        mDownloadType = downloadType;
        // network
//        int mDownloadType = SharedPreferencesUtil.getSettingInt(mLockScreenContext,
//                Constant.NETWORK_DOWNLOAD_TYPE, Constant.DownloadType.WIFI_UPDATE.ordinal());

        LogUtil.d("HttpRequest updateImage start ----------------> mDownloadType:" + mDownloadType + ", url:" + url);
        if (mDownloadType == Constant.DownloadType.NO_UPDATE.ordinal()) {
            LogUtil.d("updateImage() --------> NO_UPDATE ");
            return;
        } else if (mDownloadType == Constant.DownloadType.WIFI_UPDATE.ordinal()) {
            if (!isWifiConnected()) {
                LogUtil.d("updateImage() --------> WIFI_UPDATE, current is not wifi ");
                return;
            } else {
                LogUtil.d("updateImage() --------> WIFI_UPDATE, current is wifi, updateImage ");
            }
        } else if (mDownloadType == Constant.DownloadType.AUTO.ordinal()) {
            LogUtil.d("updateImage() --------> AUTO");
        }

        updateImageAlways(w, h, url);
    }

    public void updateImageAlways(final int w, final int h, final String url){
        if (!isAvailable()){
            LogUtil.d("updateImageAlways() --------> isAvailable is false");
            return ;
        }
        mWidth = w;
        mHeight = h;
        if (mUpdateImageThread == null) {
            mUpdateImageThread = new UpdateImageThread();
        }
        if (url != null){
            mUrl = Constant.BASE_URL + url;
        } else {
            mUrl = Constant.URL;
        }
        new Thread(mUpdateImageThread).start();
    }

    private class UpdateImageThread implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                LogUtil.d("HttpRequest httpGet start ---------------->");
                String channel_type = ReflUtils.SystemProperties.get("ro.sys.channeltype", "unknown");
                String channel = ReflUtils.SystemProperties.get("ro.sys.partner", "unknown");
                String str = new StringBuffer("&").append("width=").append(mWidth).append("&height=").append(mHeight)
                        .append("&channel_type=").append(channel_type).append("&channel=").append(channel).toString();
                HttpUtils.HttpResponse httpResponse = HttpUtils.httpGet(mContext, mUrl + str);
                Response response = mJsonParse.parseResponse(httpResponse.response, mContext);

                int wallpaperCount = 0;
                if (response != null) {
                    LogUtil.d(response.toString());

                    updateRecommendAppsIcon(response);
                    wallpaperCount = updateWallpaper(response, httpResponse.expires);
                }

                FileUtils.deleteWallpaper();
                Intent intent = new Intent(Constant.REFRESH_DATA_COMPLETE_ACTION);
                intent.putExtra("wallpaperCount", wallpaperCount);
                mContext.sendBroadcast(intent);
            }
        }
    }

    private int updateWallpaper(Response response, long expires) {
        if (response.images == null || response.images.length == 0) {
            return -1;
        }

        int oldCount = FileUtils.getWallpaperCount();
        for (int i = 0, N = response.images.length; i < N; i++) {
            int index = response.images[i].lastIndexOf("/");
            if (index != -1 && index + 1 < response.images[i].length()) {
                String fileName = response.images[i].substring(index + 1, response.images[i].length());
                if (!FileUtils.isExists(Constant.WALLPAPER_PATH + File.separator + fileName)) {
                    HttpUtils.httpGet(mContext, response.images[i], Constant.WALLPAPER_PATH, fileName);
                } else {
                    LogUtil.d(" updateWallpaper -----------------> " + fileName + " is exists");
                }
            }
        }
        int newCount = FileUtils.getWallpaperCount();

        if (expires != -1/* && (newCount - oldCount >= response.images.length)*/) {
            Settings.System.putLong(mContext.getContentResolver(), Constant.WALLPAPER_DOWNLOAD_EXPIRES, expires);
        }
        LogUtil.d("httpGet images download complete -----------------> expires:" + expires + ", wallpapercount:" + (newCount - oldCount));
        return newCount - oldCount;
    }

    private void updateRecommendAppsIcon(Response response) {
        if (response.recommendApps != null && response.recommendApps.size() > 0) {
            if (mDbUtil == null) {
                mDbUtil = new DbUtil(mContext);
            }

            mDbUtil.insert(response.recommendApps);

            List<RecommendApp> recommendAppsList = response.recommendApps;

            if (recommendAppsList == null || recommendAppsList.size() == 0) {
                return;
            }

            for (int i = 0, N = recommendAppsList.size(); i < N; i++) {
                String[] split = recommendAppsList.get(i).icon_url.split(File.separator);
                if (split.length <= 0) {
                    break;
                }
                if (!FileUtils.isExists(Constant.WALLPAPER_PATH + File.separator + recommendAppsList.get(i).icon_name)) {
                    HttpUtils.httpGet(mContext, recommendAppsList.get(i).icon_url, Constant.RECOMMENDAPP_ICON_PATH, recommendAppsList.get(i).icon_name);
                } else {
                    LogUtil.d(" updateRecommendAppsIcon -----------------> " + recommendAppsList.get(i).icon_name + " is exists");
                }
            }

            for (int i = 0, N = recommendAppsList.size(); i < N; i++) {

//                mBitmapCache.put(recommendAppsList.get(i).icon_name);
            }
            LogUtil.d("httpGet recommendappsIcon download complete -----------------> ");
        }
    }

    public boolean isAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            return cm.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo != null) {
            LogUtil.d("isWifiConnected -------> isConnected:" + networkInfo.isConnected() + "  isAvailable:" + networkInfo.isAvailable());
            return networkInfo.isConnected();
        }
        return false;
    }
}
