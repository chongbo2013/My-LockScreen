package com.lewa.lockscreen2.util;

import android.util.Log;

/**
 * Created by lewa on 3/5/15.
 */
public class LogUtil {

    public static void d(String msg) {
        if (Constant.DEBUG) {
            StackTraceElement elements = new Throwable().getStackTrace()[1];
            Log.d(Constant.TAG, elements.getFileName() + "[" + elements.getLineNumber() + "]:" + msg);
        }
    }

    public static void e(String msg) {
        if (Constant.DEBUG) {
            StackTraceElement elements = new Throwable().getStackTrace()[1];
            Log.e(Constant.TAG, elements.getFileName() + "[" + elements.getLineNumber() + "]:" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (Constant.DEBUG)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (Constant.DEBUG)
            Log.e(tag, msg);
    }
}
