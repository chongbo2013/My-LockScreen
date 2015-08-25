package com.lewa.lockscreen2.util;

import android.net.Uri;
import android.os.Environment;
import com.lewa.lockscreen2.RecommendAppInfo;

import java.io.File;

/**
 * Created by lewa on 2/10/15.
 */
public class Constant {

    public static final boolean DEBUG = true;
    public static final String TAG = "LewaLockscreen2";
    public static final String WALLPAPER_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator +
            "LEWA" + File.separator + "lockscreen" + File.separator + "wallpaper";
    public static final String RECOMMENDAPP_ICON_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator +
            "LEWA" + File.separator + "lockscreen" + File.separator + "recommendappicon";
    public static final String RECOMMENDAPP_APP = /*Environment.getExternalStorageDirectory().getPath() + File.separator +*/
            "LEWA" + File.separator + "lockscreen" + File.separator + "recommendapp";
    public static final int MAX_WALLPAPER_COUNT = 32;
    public static final int MAX_APK_COUNT = 5;
    public static final int MIN_RECOMMENDAPP_COUNT = 5;


//    public static final String BASE_URL = "http://api.beta.lewaos.com/";
    public static final String BASE_URL = "http://api.lewaos.com/";
//    public static final String URL = "http://api.beta.lewaos.com/lockscreen/layout/detail?id=1";
    public static final String URL = "http://api.lewaos.com/lockscreen/layout/detail?id=1&page=1";
    public static final String BBS_URL = "http://api.lewaos.com/apps/common/download?package=com.lewa.userfeedback&_refer=outer";

    public static final String RECOMMENDAPP_URL = "recommendapp_url";
    public static final String SHARED_PREFERENCES_BASE_NAME = "lockscreen_shardpreferences";

    public static final String WALLPAPER_ROTATION_TYPE = "WALLPAPER_ROTATION_TYPE";
    public static final String WALLPAPER_ROTATION_TIME = "wallpaper_rotation_time";

    public static final String NETWORK_DOWNLOAD_TYPE = "network_download_type";

    public static final String WALLPAPER_CURRENT_PATH = "wallpaper_current_path";
    public static final String WALLPAPER_DOWNLOAD_EXPIRES = "wallpaper_download_expires";

    public static final String SERVER_TIME_DIFF = "server_time_diff";

    public static final String WALLPAPER_ROTATION_TYPE_ACTION = "wallpaper_rotation_type_action";
    public static final String NETWORK_DOWNLOAD_TYPE_ACTION = "network_download_type_action";
    public static final String APP_MANAGER_ACTION = "app_manager_action";
    public static final String REFRESH_DATA_ACTION = "refresh_data_action";
    public static final String REFRESH_DATA_COMPLETE_ACTION = "refresh_data_complete_action";
    public static final String CHECKED_APP_MAP = "checked_app_map";
    public static final String ACTION_LAUNCH_SECURE_APP = "android.intent.action.LAUNCH_SECURE_APP";


    public static final String DEFAULT_APPMANAGER = "com.lewa.player.activity.SplashActivity#com.lewa.player#0&&"
            + "com.android.contacts.activities.ContactsEntryActivity#com.lewa.PIM#1&&"
            + "com.android.contacts.activities.MessageActivity#com.lewa.PIM#2";


    public static final long ONE_HOUR_MS = 60 * 60 * 1000;
    public static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
    public static final long SEVEN_DAY_MS = 7 * ONE_DAY_MS;

    public static final int MAX_PIC_WIDTH = 720;

    public enum WallpaperRotation {
        SCREEN_ON, ONE_HOUR, ONE_DAY
    }

    public enum DownloadType {
        NO_UPDATE, WIFI_UPDATE, AUTO
    }
}
