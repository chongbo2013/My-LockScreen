
package com.lewa.lockscreen.v5.lockscreen.activity;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.util.ConfigFile;
import com.lewa.lockscreen.laml.util.Task;
import com.lewa.lockscreen.laml.util.Utils;
import com.lewa.lockscreen.util.HapticFeedbackUtil;
import com.lewa.lockscreen.util.RUtil;
import com.lewa.lockscreen.v5.lockscreen.FancyLockScreenView;
import com.lewa.lockscreen.v5.lockscreen.LockScreenRoot.UnlockerCallback;
import com.android.internal.widget.LockPatternUtils;
public class FancyLockScreen extends FrameLayout implements UnlockerCallback {

    private static final boolean DBG = false;

    private static final String COMMAND_PAUSE = "pause";

    private static final String COMMAND_RESUME = "resume";

    private static final String OWNER_INFO_VAR = "owner_info";

    private static final String TAG = "FancyLockScreen";

    private static HapticFeedbackUtil mHapticFeedbackUtil;

    private static final SimpleRootHolder mRootHolder = new SimpleRootHolder();

    private static long sStartTime;

    private static long sTotalWakenTime;

    private boolean isPaused;

    private AudioManager mAudioManager;

    private ConfigFile mConfig;

    private LockPatternUtils mLockPatternUtils;

    private FancyLockScreenView mLockscreenView;

    private long mWakeStartTime;
    
    private final Context mContext;

    public FancyLockScreen(Context context) {
        super(context);
        mContext=context;
        isPaused = false;
        setFocusable(true);
        setFocusableInTouchMode(true);
        if (mHapticFeedbackUtil == null)
            mHapticFeedbackUtil = new HapticFeedbackUtil(context, true);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mLockPatternUtils = new LockPatternUtils(mContext);
        mRootHolder.init(mContext, this);

        ContentResolver resolver = getContext().getContentResolver();
        boolean ownerInfoEnabled = Settings.Secure.getInt(resolver,
                "lock_screen_owner_info_enabled", 1) != 0;
        String ownerString = ownerInfoEnabled ? Settings.Secure.getString(resolver,
                "lock_screen_owner_info") : null;
        Utils.putVariableString(OWNER_INFO_VAR, mRootHolder.getContext().mVariables, ownerString);
        openOrClosedFancyWeather(context);
        mRootHolder.getRoot().setUnlockerCallback(this);
        mLockscreenView = mRootHolder.createView(mContext);
        addView(mLockscreenView);
        mWakeStartTime = System.currentTimeMillis() / 1000;
        if (sStartTime == 0)
            sStartTime = mWakeStartTime;
        // TODO: update battery info by service
        onRefreshBatteryInfo(false, false, 100);
        onPause();
    }

    private static final String OPEN_DYNAWEATHER = "isOpenDynaWeather";

    private void openOrClosedFancyWeather(Context context){
        boolean fancyWeatherEnabled = isOpenFancyWeather(context);
        Utils.putVariableNumber(OPEN_DYNAWEATHER,  mRootHolder.getContext().mVariables,(double)(fancyWeatherEnabled ? 0: -1));
    }

    public static boolean isOpenFancyWeather(Context context){
        return Settings.Secure.getInt(context.getContentResolver(), "lock_screen_fancy_weather_enabled", 1) >= 1 ;
    }
 
    public static void clearCache() {
        mRootHolder.clear();
        ThemeResources.reset();
    }

    public void cleanUp() {
        mRootHolder.cleanUp(this);
        mLockPatternUtils = null;
        if (DBG)
            Log.d(TAG, "cleanUp, isPaused: " + isPaused);
        System.gc();
    }

    public void cleanUpView() {
        mLockscreenView.cleanUp(true);
    }

    public String getProperty(String name) {
        return mRootHolder.getRoot().getRawAttr(name);
    }

    public Task findTask(String id) {
        if (mConfig == null)
            return null;
        else
            return mConfig.getTask(id);
    }

    public void haptic(int effectId) {
        mHapticFeedbackUtil.performHapticFeedback(effectId, false);
    }

    public boolean isDisplayDesktop() {
        return mRootHolder.getRoot().isDisplayDesktop();
    }

    public boolean isSecure() {
        return mLockPatternUtils.isSecure();
    }

    public boolean isSoundEnable() {
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL
                && Settings.System.getInt(mContext.getContentResolver(),
                        "lockscreen_sounds_enabled", 1) != 0;
    }

    public boolean needsInput() {
        return false;
    }

    public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel) {
        if (DBG)
            Log.d(TAG, "onRefreshBatteryInfo: " + showBatteryInfo + " " + pluggedIn + " "
                    + batteryLevel);
        if (mRootHolder.getRoot() != null)
            mRootHolder.getRoot().onRefreshBatteryInfo(showBatteryInfo, pluggedIn, batteryLevel);
    }

    public void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn) {
        if (DBG)
            Log.d(TAG, "onRefreshCarrierInfo(" + plmn + ", " + spn + ")");
    }

    public void onResume() {
        if (DBG)
            Log.d(TAG, "onResume");
        mRootHolder.getThread().setPausedSafety(false);
        mLockscreenView.onResume();
        mRootHolder.getRoot().onCommand(COMMAND_RESUME);
        isPaused = false;
        mWakeStartTime = System.currentTimeMillis() / 1000;
    }

    public void onPause() {
        if (DBG)
            Log.d(TAG, "onPause");
        mRootHolder.getThread().setPausedSafety(true);
        mLockscreenView.onPause();
        mRootHolder.getRoot().onCommand(COMMAND_PAUSE);
        isPaused = true;
        long wakenTime = System.currentTimeMillis() / 1000 - mWakeStartTime;
        sTotalWakenTime = wakenTime + sTotalWakenTime;
    }

    public void rebindView() {
        mRootHolder.getRoot().setUnlockerCallback(this);
        mLockscreenView.rebindRoot();
    }


    public void unlocked(final Intent intent, int delay) {
        unlocked(intent,delay,null,null);
    }

    @Override
    public void unlocked(final Intent intent, int delay, final String enterResName, final String exitResName) {
        postDelayed(new Runnable() {

            @Override
            public void run() {
                try {
                    if (intent != null) {
                        Context context = getContext();
                        if (!TextUtils.isEmpty(enterResName) || !TextUtils.isEmpty(exitResName)) {
                            ActivityOptions options = ActivityOptions.makeCustomAnimation(context, getResIdForName(enterResName), getResIdForName(exitResName));
                            context.startActivity(intent, options.toBundle());
                        } else {
                            context.startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                try {
                    ((LockScreenActivity)getContext()).unlock();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }, delay);
        Log.d(TAG,
              String.format("lockscreen awake time: [%d sec] in time range: [%d sec]", sTotalWakenTime,
                            System.currentTimeMillis() / 1000 - sStartTime));
    }

    public static SimpleRootHolder getRootHolder() {
        return mRootHolder;
    }

    @Override
    public void pokeWakelock() {
    }

    @Override
    public void pokeWakelock(int i) {
    }

    private RUtil mUtil;

    public int getResIdForName(String name){
        if (mUtil == null) {
            mUtil = new RUtil(getContext());
        }
        return  mUtil.getAnimAttrId(name) ;
    }

    public  Bitmap getBitmap(String name){
        return mRootHolder.getBitmap(name);
    }

}
