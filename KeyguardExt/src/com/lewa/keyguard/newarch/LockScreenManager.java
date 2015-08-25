package com.lewa.keyguard.newarch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by ning on 15-3-31.
 */
class LockScreenManager {
    private static final Intent LOCKSCREEN_INTENT = new Intent(
            "lewa.intent.action.LOCKSCREEN").addCategory(
            "lewa.intent.category.LOCKSCREEN");

    public static final String DEFAULT_LOCKSCREEN = "com.lewa.lockscreen2";

    private final LockScreenController mLockScreenController = new LockScreenController();

    private Context mContext;
    private KeyguardManager mKeyguardManager;
    private LockScreenInfo mCurrentLockScreenInfo;

    public LockScreenManager(Context context, KeyguardManager keyguardManager) {
        mContext = context;
        mKeyguardManager = keyguardManager;
        installLockScreenIfNeeded();
    }

    public LockScreenController getLockScreenController() {
        installLockScreenIfNeeded();
        return mLockScreenController;
    }

    private void installLockScreenIfNeeded() {
        String lockScreenName = Settings.System.getString(
                mContext.getContentResolver(), "lewa.theme.lockscreen");

        if (lockScreenName == null) {
            lockScreenName = DEFAULT_LOCKSCREEN;
        }

        int hasExceptions = Settings.System.getInt(
                mContext.getContentResolver(), "lewa.theme.lockscreen.status", -1);
        if (hasExceptions == 0) {
            lockScreenName = DEFAULT_LOCKSCREEN;
            Settings.System.putString(
                    mContext.getContentResolver(), "lewa.theme.lockscreen", DEFAULT_LOCKSCREEN);
            Settings.System.putInt(
                    mContext.getContentResolver(), "lewa.theme.lockscreen.status", -1);
        }

        if (mCurrentLockScreenInfo == null ||
                mCurrentLockScreenInfo.name == null ||
                !mCurrentLockScreenInfo.name.equals(lockScreenName)) {
            loadAndInstallLockScreen(lockScreenName);
        }
    }

    private void loadAndInstallLockScreen(String lockScreenName) {
        LockScreenInfo newLockScreenInfo = null;

        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> infoList = pm.queryIntentActivities(LOCKSCREEN_INTENT, 0);
        for (ResolveInfo info : infoList) {
            if (info.activityInfo.packageName.equals(lockScreenName)) {
                newLockScreenInfo = loadLockScreen(info.activityInfo.packageName, info.activityInfo.name);
                break;
            }
        }

        if (newLockScreenInfo == null || newLockScreenInfo.lockScreen == null) {
            return;
        }

        if (mCurrentLockScreenInfo != null && mCurrentLockScreenInfo.lockScreen != null) {
            mLockScreenController.gotoState(LockScreenController.STATE_CREATED);
            LockScreen current = mCurrentLockScreenInfo.lockScreen;
            current.performDestroy();
        }
        mCurrentLockScreenInfo = null;

        LockScreen next = newLockScreenInfo.lockScreen;
        try {
            next.attach(newLockScreenInfo.apkContext, mContext, mKeyguardManager);
            next.performCreate();
            mLockScreenController.setLockScreen(newLockScreenInfo.lockScreen);
        } catch (Exception e) {
            Settings.System.putInt(
                    mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
            throw new RuntimeException(e);
        }

        mCurrentLockScreenInfo = newLockScreenInfo;
    }

    private LockScreenInfo loadLockScreen(String packageName, String activityName) {
        Context apkContext = null;
        Object lockScreenObj = null;
        try {
            apkContext = mContext.createPackageContext(
                    packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            Class<?> activityClass = apkContext.getClassLoader().loadClass(activityName);
            lockScreenObj = activityClass.newInstance();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        LockScreenInfo info = new LockScreenInfo();
        info.apkContext = apkContext;
        info.lockScreen = (LockScreen) InterfaceCast.cast(LockScreen.class, lockScreenObj);
        info.name = packageName;

        return info;
    }

    public static class LockScreenInfo {
        public Context apkContext;
        public LockScreen lockScreen;
        public String name;
    }

    class LockScreenController {
        private static final int STATE_CREATED = 0;
        private static final int STATE_STARTED = 1;
        private static final int STATE_RUNNING = 2;

        private WeakReference<LockScreen> mLockScreen;
        private WeakReference<OnRootViewChangeListener> mListener;

        private int mState = STATE_CREATED;
        private boolean mBouncerOpened = false;

        public View getRootView() {
            LockScreen lockScreen = mLockScreen.get();
            if (lockScreen != null) {
                return lockScreen.getRootView();
            }
            return null;
        }

        public void setOnRootViewChangeListener(OnRootViewChangeListener listener) {
            mListener = new WeakReference<OnRootViewChangeListener>(listener);
        }

        public void gotoState(int state) {
            if (mState == state) {
                return;
            }
            switch (mState) {
                case STATE_CREATED:
                    if (state == STATE_STARTED) {
                        performStart();
                        break;
                    }
                    if (state == STATE_RUNNING) {
                        performStart();
                        performResume();
                        break;
                    }
                case STATE_STARTED:
                    if (state == STATE_RUNNING) {
                        performResume();
                        break;
                    }
                    if (state == STATE_CREATED) {
                        performStop();
                        break;
                    }
                case STATE_RUNNING:
                    if (state == STATE_CREATED) {
                        performPause();
                        performStop();
                        break;
                    }
                    if (state == STATE_STARTED) {
                        performPause();
                        break;
                    }
                default:
            }
        }

        public void onBouncerOpened() {
            try {
                if (!mBouncerOpened && mState != STATE_CREATED) {
                    mBouncerOpened = true;
                    LockScreen lockScreen = mLockScreen.get();
                    if (lockScreen != null) {
                        lockScreen.onBouncerOpened();
                    }
                }
            } catch (Exception e) {
                Settings.System.putInt(
                        mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
                throw new RuntimeException(e);
            }
        }

        public void onBouncerClosed() {
            try {
                if (mBouncerOpened && mState != STATE_CREATED) {
                    mBouncerOpened = false;
                    LockScreen lockScreen = mLockScreen.get();
                    if (lockScreen != null) {
                        lockScreen.onBouncerClosed();
                    }
                }
            } catch (Exception e) {
                Settings.System.putInt(
                        mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
                throw new RuntimeException(e);
            }
        }

        private void setLockScreen(LockScreen lockScreen) {
            mLockScreen = new WeakReference<LockScreen>(lockScreen);
            if (mListener != null) {
                OnRootViewChangeListener l = mListener.get();
                if (l != null) {
                    l.onRootViewChanged(lockScreen.getRootView());
                }
            }
            if (getRootView() != null) {
                getRootView().setVisibility(View.GONE);
            }
            mState = STATE_CREATED;
        }

        private void performStart() {
            installLockScreenIfNeeded();

            try {
                LockScreen lockScreen = mLockScreen.get();
                if (lockScreen != null) {
                    lockScreen.performStart();
                }
                mBouncerOpened = false;
                mState = STATE_STARTED;
            } catch (Exception e) {
                Settings.System.putInt(
                        mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
                throw new RuntimeException(e);
            }
        }

        private void performResume() {
            try {
                LockScreen lockScreen = mLockScreen.get();
                if (lockScreen != null) {
                    lockScreen.performResume();
                }
                if (getRootView() != null) {
                    getRootView().setVisibility(View.VISIBLE);
                }
                mState = STATE_RUNNING;
            } catch (Exception e) {
                Settings.System.putInt(
                        mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
                throw new RuntimeException(e);
            }
        }

        private void performPause() {
            try {
                LockScreen lockScreen = mLockScreen.get();
                if (lockScreen != null) {
                    Thread.sleep(120, 0); // hiding slowly
                    lockScreen.performPause();
                }
                if (getRootView() != null) {
                    getRootView().setVisibility(View.GONE);
                }
                mState = STATE_STARTED;
            } catch (Exception e) {
                Settings.System.putInt(
                        mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
                throw new RuntimeException(e);
            }
        }

        private void performStop() {
            try {
                LockScreen lockScreen = mLockScreen.get();
                if (lockScreen != null) {
                    lockScreen.performStop();
                }
                mState = STATE_CREATED;
            } catch (Exception e) {
                Settings.System.putInt(
                        mContext.getContentResolver(), "lewa.theme.lockscreen.status", 0);
                throw new RuntimeException(e);
            }
        }
    }

    public static interface OnRootViewChangeListener {
        public void onRootViewChanged(View view);
    }
}
