package com.lewa.lockscreen2.util;

import java.lang.reflect.Method;


public class ReflUtils {
    private static Method sMSystemProperties_get = null;
    private static Method sMSystemProperties_getLong = null;

    public static class SystemProperties {
        public static String get(String key, String def) {
            try {
                if (sMSystemProperties_get == null) {
                    Class<?> systemPropertiesClz = Class.forName("android.os.SystemProperties");
                    sMSystemProperties_get = systemPropertiesClz.getMethod(
                            "get", String.class, String.class);
                }
                if (sMSystemProperties_get != null) {
                    return (String) sMSystemProperties_get.invoke(null, key, def);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return def;
        }
    
        public static long getLong(String key, long def) {
            try {
                if (sMSystemProperties_getLong == null) {
                    Class<?> systemPropertiesClz = Class.forName("android.os.SystemProperties");
                    sMSystemProperties_getLong = systemPropertiesClz.getMethod(
                            "getLong", String.class, Long.TYPE);
                }
                if (sMSystemProperties_getLong != null) {
                    return (Long) sMSystemProperties_getLong.invoke(null, key, def);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return def;
        }
    }
}
