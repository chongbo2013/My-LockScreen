
package com.lewa.lockscreen2.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    public static String getSetting(Context context, String name) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        return setting.getString(name, "");
    }

    public static int getSettingInt(Context context, String name, int defaultValue) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        return setting.getInt(name, defaultValue);
    }

    public static float getSettingFloat(Context context, String name) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        return setting.getFloat(name, 0.0f);
    }

    public static long getSettingLong(Context context, String name) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        return setting.getLong(name, 0);
    }

    public static boolean getSettingBoolean(Context context, String name) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        return setting.getBoolean(name, false);
    }

    public static void setSetting(Context context, String name, String value) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static void setSetting(Context context, String name, int value) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static void setSetting(Context context, String name, float value) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putFloat(name, value);
        editor.commit();
    }

    public static void setSetting(Context context, String name, long value) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putLong(name, value);
        editor.commit();
    }

    public static void setSetting(Context context, String name, boolean value) {
        SharedPreferences setting = context.getSharedPreferences(
                Constant.SHARED_PREFERENCES_BASE_NAME, Activity.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }
}
