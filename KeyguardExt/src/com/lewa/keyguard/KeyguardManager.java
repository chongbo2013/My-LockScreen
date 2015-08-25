package com.lewa.keyguard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class KeyguardManager {
    public abstract void dismiss();
    public abstract void userActivity();
    public abstract void launchActivity(Intent intent, boolean secure);
    public abstract void launchActivity(Intent intent, Bundle animation, boolean secure);
    public abstract void launchCamera();

    public static KeyguardManager getInstance(Context context) {
        ClassLoader contextEnv = context.getClassLoader();
        ClassLoader localEnv = KeyguardManager.class.getClassLoader();

        if (contextEnv.equals(localEnv)) {
            return KeyguardManagerBinder.instance();
        } else {
            Class<?> clazz = null;
            Object obj = null;

            try {
                clazz = contextEnv.loadClass(KeyguardManagerBinder.class.getName());
                Method instanceMethod = clazz.getDeclaredMethod("instance");
                instanceMethod.setAccessible(true);
                obj = instanceMethod.invoke(null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return new KeyguardManagerProxy(clazz, obj);
        }
    }

    static class KeyguardManagerProxy extends KeyguardManager {
        Object mObject;

        Method mDismiss;
        Method mUserActivity;
        Method mLaunchActivity;
        Method mLaunchActivity2;
        Method mLaunchCamera;

        KeyguardManagerProxy(Class<?> implClass, Object impl) {
            if (implClass != null && impl != null) {
                mObject = impl;
                try {
                    mDismiss = implClass.getMethod("dismiss");
                    mUserActivity = implClass.getMethod("userActivity");
                    mLaunchActivity = implClass.getMethod("launchActivity", Intent.class, Boolean.TYPE);
                    mLaunchActivity2 = implClass.getMethod("launchActivity", Intent.class, Bundle.class, Boolean.TYPE);
                    mLaunchCamera = implClass.getMethod("launchCamera");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void dismiss() {
            if (mDismiss != null) {
                try {
                    mDismiss.invoke(mObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void userActivity() {
            if (mUserActivity != null) {
                try {
                    mUserActivity.invoke(mObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void launchActivity(Intent intent, boolean secure) {
            if (mLaunchActivity != null) {
                try {
                    mLaunchActivity.invoke(mObject, intent, secure);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void launchActivity(Intent intent, Bundle animation, boolean secure) {
            if (mLaunchActivity2 != null) {
                try {
                    mLaunchActivity2.invoke(mObject, intent, animation, secure);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void launchCamera() {
            if (mLaunchCamera!= null) {
                try {
                    mLaunchCamera.invoke(mObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
