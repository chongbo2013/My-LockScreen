package com.lewa.lockscreen.impl;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.lewa.keyguard.newarch.KeyguardManager;
import com.lewa.lockscreen.content.res.ThemeConstants;
import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.LifecycleResourceManager;
import com.lewa.lockscreen.laml.RenderThread;
import com.lewa.lockscreen.laml.ScreenContext;
import com.lewa.lockscreen.laml.util.ConfigFile;
import com.lewa.lockscreen.laml.util.Task;
import com.lewa.lockscreen.laml.util.Utils;
import com.lewa.lockscreen.util.RUtil;
import com.lewa.lockscreen.v5.lockscreen.FancyLockScreenView;
import com.lewa.lockscreen.v5.lockscreen.LockScreenElementFactory;
import com.lewa.lockscreen.v5.lockscreen.LockScreenResourceLoader;
import com.lewa.lockscreen.v5.lockscreen.LockScreenRoot;
import lewa.util.ImageUtils;

public class LamlLockScreen2 extends FrameLayout {
    private static final String TAG = "LamlLockScreen";

    private static final int LOCKSCREEN_WALLPAPER_FLAG = -100;

    private static final String COMMAND_PAUSE = "pause";
    private static final String COMMAND_RESUME = "resume";

    private static final String OWNER_INFO_VAR = "owner_info";
    private static final String OPEN_LIST_MSG = "isOpenListMsg";
    private static final String OPEN_DYNA_WEATHER = "isOpenDynaWeather";
    private static final String FAHRENHEIT_DEGRESS = "isFahrenheitDegrees";
    private static final String CURRENT_LANGUAGE_FLAG = "current_language";

    private LockScreenResourceLoader mResourceLoader;
    private LifecycleResourceManager mResourceMgr;
    private RenderThread mRenderThread;
    private ScreenContext mScreenContext;

    private LockScreenRoot mLockScreenRoot;
    private FancyLockScreenView mLockScreenView;
    private ConfigFile mConfig;
    private int mThemeChanged = -1;

    private KeyguardManager mKeyguardManager;
    private boolean mIsKeyguardShowed;

    private Drawable mDismissBk;

    public LamlLockScreen2(Context context) {
        this(context, null);
    }

    public LamlLockScreen2(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public LamlLockScreen2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate();
    }

    private void onCreate() {
        mResourceLoader = new LockScreenResourceLoader();
        mResourceLoader.setLocal(getResources().getConfiguration().locale);
        mResourceMgr = new LifecycleResourceManager(mResourceLoader,
                LifecycleResourceManager.TIME_DAY, LifecycleResourceManager.TIME_HOUR);
        mScreenContext = new ScreenContext(getContext(), mResourceMgr, new LockScreenElementFactory());
        mRenderThread = new RenderThread();
        mRenderThread.start();

        createLockScreenRoot();
    }

    private void createLockScreenRoot() {
        mLockScreenRoot = new LockScreenRoot(mScreenContext);
        mLockScreenRoot.setConfig(ThemeConstants.CONFIG_EXTRA_PATH);
        mLockScreenRoot.load();
        mLockScreenRoot.setUnlockerCallback(new UnlockerCallback());
        mScreenContext.mRoot = mLockScreenRoot;
        updateConfig(mScreenContext);
    }

    private void updateConfig(ScreenContext screenContext) {
        mConfig = new ConfigFile();

        if (!mConfig.load(ThemeConstants.CONFIG_EXTRA_PATH)) {
            mConfig = null;
        } else {
            for (ConfigFile.Variable v : mConfig.getVariables()) {
                if (TextUtils.equals(v.type, "string")) {
                    Utils.putVariableString(v.name, screenContext.mVariables, v.value);
                } else if (TextUtils.equals(v.type, "number")) {
                    try {
                        Utils.putVariableNumber(v.name, screenContext.mVariables, Double.parseDouble(v.value));
                    } catch (NumberFormatException numberformatexception) {
                    }
                }
            }
            for (Task t : mConfig.getTasks()) {
                Utils.putVariableString(t.id, "name", screenContext.mVariables, t.name);
                Utils.putVariableString(t.id, "package", screenContext.mVariables, t.packageName);
                Utils.putVariableString(t.id, "class", screenContext.mVariables, t.className);
            }
        }

        updateOwnerInfoConfig(screenContext);
        updateWeatherConfig(screenContext);
        updateMsgConfig(screenContext);
        updateLanguageConfig(screenContext);
    }

    private void updateOwnerInfoConfig(ScreenContext screenContext) {
        ContentResolver resolver = getContext().getContentResolver();

        boolean ownerInfoEnabled = Settings.Secure.getInt(resolver, "lock_screen_owner_info_enabled", 1) != 0;
        String ownerString = ownerInfoEnabled ? Settings.Secure.getString(resolver, "lock_screen_owner_info") : null;
        Utils.putVariableString(OWNER_INFO_VAR, screenContext.mVariables, ownerString);
    }

    private void updateWeatherConfig(ScreenContext screenContext) {
        ContentResolver resolver = getContext().getContentResolver();

        boolean fancyWeatherEnabled = Settings.Secure.getInt(resolver, "lock_screen_fancy_weather_enabled", 1) >= 1;
        Utils.putVariableNumber(OPEN_DYNA_WEATHER, screenContext.mVariables, (double) (fancyWeatherEnabled ? 0 : -1));

        boolean isFahrenheit = Settings.System.getInt(resolver, "temp_unit", 0) >= 1;
        Utils.putVariableNumber(FAHRENHEIT_DEGRESS, screenContext.mVariables, (double) (isFahrenheit ? 0 : -1));
    }

    private void updateMsgConfig(ScreenContext screenContext) {
        ContentResolver resolver = getContext().getContentResolver();

        boolean listMsgEnabled = Settings.Secure.getInt(resolver, "lock_screen_list_msg_enabled", 1) >= 1;
        Utils.putVariableNumber(OPEN_LIST_MSG, screenContext.mVariables, (double) (listMsgEnabled ? 0 : 1));
    }

    private void updateLanguageConfig(ScreenContext screenContext) {
        Utils.putVariableNumber(CURRENT_LANGUAGE_FLAG, screenContext.mVariables, (double) Utils.getCurrentLanguage());
    }

    private boolean isWallpaperChanged() {
        int version = SystemProperties.getInt("sys.lewa.themeChanged", -1);
        if (version > mThemeChanged) {
            mThemeChanged = version;
            return true;
        }
        return false;
    }

    private boolean isThemeChanged() {
        int version = SystemProperties.getInt("sys.lewa.themeChanged", -1);
        if (version == LOCKSCREEN_WALLPAPER_FLAG) {
            ThemeResources.clearLockWallpaperCache();
        }

        if (version > mThemeChanged) {
            mThemeChanged = version;
            return true;
        }
        return false;
    }

    private Drawable getBlurredBackground() {
        int w = Resources.getSystem().getDisplayMetrics().widthPixels;
        int h = Resources.getSystem().getDisplayMetrics().heightPixels;

        Bitmap screenshot = SurfaceControl.screenshot(w / 8, h / 8);
        if (screenshot == null ||
                (screenshot.getPixel(0, 0) & 0xFF000000) == 0) {
            screenshot = SurfaceControl.screenshot(w / 8, h / 8);
        }
        // Try again
        if (screenshot == null ||
                (screenshot.getPixel(0, 0) & 0xFF000000) == 0) {
            return null;
        }
        Bitmap in = Bitmap.createScaledBitmap(screenshot, 64, 113, true);
        Bitmap out = Bitmap.createBitmap(64, 113, Bitmap.Config.ARGB_8888);
        ImageUtils.fastBlur(in, out, 12);
        screenshot.recycle();

        BitmapDrawable blurDrawable = new BitmapDrawable(Resources.getSystem(), out);
        blurDrawable.setFilterBitmap(true);
        return  blurDrawable;
    }

    public Drawable getDismissBackground() {
        return mDismissBk;
    }

    public void onAttachToKeyguard(KeyguardManager keyguardManager) {
        mKeyguardManager = keyguardManager;

        if (isThemeChanged()) {
            ThemeResources.reset();
            mResourceMgr.clear();
            createLockScreenRoot();
        } else {
            updateConfig(mScreenContext);
        }

        mLockScreenView = new FancyLockScreenView(getContext(), mLockScreenRoot, mRenderThread);
        addView(mLockScreenView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void onDetachFromKeyguard() {
        removeView(mLockScreenView);
        if (mLockScreenView != null) {
            mLockScreenView.cleanUp(true);
        }
        mLockScreenView = null;
        mKeyguardManager = null;

        System.gc();
    }

    public void showKeyguard() {
        mIsKeyguardShowed = true;
        mRenderThread.setPausedSafety(false);
        mLockScreenView.setAlpha(1);
        mLockScreenView.setVisibility(VISIBLE);
        mLockScreenRoot.onCommand(COMMAND_RESUME);
    }

    public void hideKeyguard() {
        mIsKeyguardShowed = false;
        mRenderThread.setPausedSafety(true);
        mLockScreenView.setVisibility(INVISIBLE);
        mLockScreenRoot.onCommand(COMMAND_PAUSE);
    }

    public void onBouncerShow() {
//        if (mIsKeyguardShowed) {
//            mLockScreenView.onPause();
//            mLockScreenView.setAlpha(0);
//            mLockScreenRoot.reset();
//        }
        //mLockScreenView.onPause();
        hideKeyguard();
    }

    public void onBouncerHide() {
        showKeyguard();
//        mLockScreenView.onPause();
//        mLockScreenRoot.reset();
//        mLockScreenView.onResume();

//        if (mIsKeyguardShowed) {
//            mLockScreenRoot.reset();
//            mLockScreenView.setAlpha(1);
//            mLockScreenView.onResume();
//        }
    }

    public void cleanUp() {
        if (mLockScreenView != null) {
            mLockScreenView.cleanUp();
        }
        mResourceMgr.clear();
        mRenderThread.setStop();
        ThemeResources.reset();
    }

    private class UnlockerCallback implements LockScreenRoot.UnlockerCallback {

        @Override
        public Task findTask(String s) {
            return null;
        }

        @Override
        public void haptic(int i) {

        }

        @Override
        public boolean isSecure() {
            return mKeyguardManager.isSecure();
        }

        @Override
        public boolean isSoundEnable() {
            return false;
        }

        @Override
        public void pokeWakelock() {
            pokeWakelock(-1);
        }

        @Override
        public void pokeWakelock(int i) {
            if (mKeyguardManager != null) {
                mKeyguardManager.userActivity();
            }
        }

        @Override
        public void unlocked(Intent intent, int delay) {
            unlocked(intent, delay, null, null);
        }

        @Override
        public void unlocked(Intent intent, int delay,
                             final String enterResName, final String exitResName) {
            if (mKeyguardManager == null) {
                return;
            }

            if (mKeyguardManager.isSecure()) {
                Drawable dismissBk = getBlurredBackground();
                if (dismissBk != null) {
                    mDismissBk = dismissBk;
                }
            }

            if (intent == null) {
                dismiss();
                return;
            } else {
                if (intent.getAction().endsWith("STILL_IMAGE_CAMERA")) {
                    launchCamera();
                    return;
                }

                if (!TextUtils.isEmpty(enterResName)
                        || !TextUtils.isEmpty(exitResName)) {
                    RUtil rUtil = new RUtil(getContext());
                    int inResId = rUtil.getAnimAttrId(enterResName);
                    int outResId = rUtil.getAnimAttrId(exitResName);
                    Bundle animation = ActivityOptions.makeCustomAnimation(
                            getContext(), inResId, outResId).toBundle();
                    launchActivity(intent, animation);
                } else {
                    launchActivity(intent);
                }
            }
        }

        @Override
        public String getProperty(String name) {
            return null;
        }

        private void launchActivity(final Intent intent) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mKeyguardManager != null){
                        mKeyguardManager.launchActivity(intent, false);
                    }
                }
            }, 100);
        }

        private void launchActivity(final Intent intent, final Bundle animation) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mKeyguardManager != null){
                        mKeyguardManager.launchActivity(intent, animation, false);
                    }
                }
            }, 100);
        }

        private void launchCamera() {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mKeyguardManager != null){
                        mKeyguardManager.launchCamera();
                    }
                }
            }, 100);
        }

        private void dismiss() {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mKeyguardManager != null){
                        mKeyguardManager.dismiss();
                    }
                }
            }, 100);
        }
    }
}
