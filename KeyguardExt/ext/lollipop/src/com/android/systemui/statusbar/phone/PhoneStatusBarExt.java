package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.lewa.keyguard.newarch.*;

import java.util.List;

/**
 * This class manage the LockScreen creating, showing and all the stuff.
 * It's a replacement of PhoneStatusBar in lollipop.
 */
public class PhoneStatusBarExt {
    private static final String TAG = "KeyguardLockScreen";
    private static final boolean DEBUG = true;

    private static final Intent SECURE_LEWA_CAMERA_INTENT =
            new Intent("lewa.media.action.STILL_IMAGE_CAMERA_SECURE")
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    private static final Intent INSECURE_LEWA_CAMERA_INTENT =
            new Intent("lewa.media.action.STILL_IMAGE_CAMERA");

    private static final Intent SECURE_CAMERA_INTENT =
            new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    private static final Intent INSECURE_CAMERA_INTENT =
            new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);

    private Context mContext;
    private ViewMediatorCallback mCallback;
    private LockPatternUtils mLockPatternUtils;
    private StatusBarWindowManager mWindowManager;
    private StatusBarKeyguardViewManager mViewManager;
    private PhoneStatusBar mPhoneStatusBar;
    private ViewGroup mContainer;

    private KeyguardUpdateMonitor mUpdateMonitor;
    private FlashlightController mFlashLightController;

    private LockScreenRootView mRoot;

    private boolean mKeyguardFadingAway;
    private long mKeyguardFadingAwayDelay;
    private long mKeyguardFadingAwayDuration;

    /**
     * The 'remote' LockScreen will use this KeyguardManager to control the Keyguard.
     */
    private final KeyguardManager mKeyguardManager = new KeyguardManager() {
        @Override
        public void dismiss() {
            mViewManager.dismiss();
        }

        @Override
        public void userActivity() {
            mCallback.userActivity();
        }

        @Override
        public void launchActivity(Intent intent, boolean secure) {
            if (!secure) {
                mPhoneStatusBar.startActivity(intent, false);
            } else {
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                );
                mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            }
        }

        @Override
        public void launchActivity(Intent intent, Bundle animation, boolean secure) {
            if (!secure) {
                mPhoneStatusBar.startActivity(intent, false);
            } else {
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                );
                mContext.startActivityAsUser(intent, animation, UserHandle.CURRENT);
            }
        }

        @Override
        public void launchCamera() {
            mFlashLightController.killFlashlight();
            Intent intent = getCameraIntent();
            if (intent == SECURE_CAMERA_INTENT || intent == SECURE_LEWA_CAMERA_INTENT) {
                //mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                mPhoneStatusBar.startActivity(INSECURE_LEWA_CAMERA_INTENT, false);
            } else {
                mPhoneStatusBar.startActivity(intent, false);
            }
        }

        @Override
        public void setNeedsInput(boolean needsInput) {
            mViewManager.setNeedsInput(needsInput);
        }

        @Override
        public boolean isSecure() {
            return mViewManager.isSecure();
        }
    };

    private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
            super.onRefreshBatteryInfo(status);
        }
    };

    private boolean mIsScreenTurnedOn;
    private boolean mIsKeyguardShowing;

    public PhoneStatusBarExt(Context context, ViewMediatorCallback callback,
                             LockPatternUtils lockPatternUtils, StatusBarWindowManager windowManager,
                             StatusBarKeyguardViewManager viewManager, PhoneStatusBar phoneStatusBar,
                             ViewGroup container) {
        mContext = context;
        mCallback = callback;
        mLockPatternUtils = lockPatternUtils;
        mWindowManager = windowManager;
        mViewManager = viewManager;
        mPhoneStatusBar = phoneStatusBar;
        mContainer = container;

        mUpdateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        mUpdateMonitor.registerCallback(mKeyguardUpdateMonitorCallback);

        mFlashLightController = new FlashlightController(mContext);
        mFlashLightController.initialize();

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        View scrimView = mContainer.findViewById(R.id.scrim_behind);
        int scrimPos =  mContainer.indexOfChild(scrimView);

        mRoot = new LockScreenRootView(mContext, mKeyguardManager);
        mContainer.addView(mRoot, scrimPos + 1, lp);
    }

    public void setKeyguardFadingAway(long delay, long fadeoutDuration) {
        if (DEBUG) Log.d(TAG, "setKeyguardFadingAway, delay="+
                delay + ", fadeoutDuration=" + fadeoutDuration);
        mKeyguardFadingAway = true;
        mKeyguardFadingAwayDelay = delay;
        mKeyguardFadingAwayDuration = fadeoutDuration;
        updateKeyguardStates();
    }

    public void finishKeyguardFadingAway() {
        if (DEBUG) Log.d(TAG, "finishKeyguardFadingAway");
        mKeyguardFadingAway = false;
    }

    public boolean isKeyguardFadingAway() {
        if (DEBUG) Log.d(TAG, "isKeyguardFadingAway, ret=" +
                mKeyguardFadingAway);
        return mKeyguardFadingAway;
    }

    public long getKeyguardFadingAwayDelay() {
        if (DEBUG) Log.d(TAG, "getKeyguardFadingAwayDelay, ret=" +
                mKeyguardFadingAwayDelay);
        return mKeyguardFadingAwayDelay;
    }

    public void onScreenTurnedOn() {
        if (DEBUG) Log.d(TAG, "onScreenTurnedOn");
        mIsScreenTurnedOn = true;
        updateKeyguardStates();
    }

    public void onScreenTurnedOff() {
        if (DEBUG) Log.d(TAG, "onScreenTurnedOff");
        mPhoneStatusBar.makeExpandedInvisible();
        mIsScreenTurnedOn = false;
        updateKeyguardStates();
    }

    public void showKeyguard() {
        if (DEBUG) Log.d(TAG, "showKeyguard");
        mPhoneStatusBar.makeExpandedInvisible();
        mIsKeyguardShowing = true;
        updateKeyguardStates();
    }

    public boolean hideKeyguard() {
        if (DEBUG) Log.d(TAG, "hideKeyguard");
        mPhoneStatusBar.makeExpandedInvisible();
        mIsKeyguardShowing = false;
        updateKeyguardStates();
        return false;
    }

    public void setBouncerShowing(boolean bouncerShowing) {
        if (DEBUG) Log.d(TAG, "setBouncerShowing, bouncerShowing=" +
                bouncerShowing);
        if (bouncerShowing) {
            mRoot.onBouncerOpened();
        } else {
            mRoot.onBouncerClosed();
        }
    }

    public boolean isInLaunchTransition() {
        if (DEBUG) Log.d(TAG, "isInLaunchTransition, ret=" + false);
        return false;
    }

    public void fadeKeyguardAfterLaunchTransition(final Runnable beforeFading,
                                                  final Runnable endRunnable) {
        if (DEBUG) Log.d(TAG, "fadeKeyguardAfterLaunchTransition, beforeFading=" +
                beforeFading + ", endRunnable=" + endRunnable);
    }

    public boolean isGoingToNotificationShade() {
        // Useless in LewaLockscreen;
        return false;
    }

    public boolean isCollapsing() {
        // Useless in LewaLockscreen;
        return false;
    }

    public void addPostCollapseAction(Runnable r) {
        // Useless in LewaLockscreen;
    }

    public View getNavigationBarView() {
        // Useless in LewaLockscreen;
        return null;
    }

    private void updateKeyguardStates() {
        if (mKeyguardFadingAway) {
            mRoot.changeLockScreenState(LockScreenRootView.STATE_CREATED);
        } else {
            if (mIsScreenTurnedOn) {
                if (mIsKeyguardShowing) {
                    mRoot.changeLockScreenState(LockScreenRootView.STATE_RUNNING);
                } else {
                    mRoot.changeLockScreenState(LockScreenRootView.STATE_STARTED);
                }
            } else {
                if (mIsKeyguardShowing) {
                    mRoot.changeLockScreenState(LockScreenRootView.STATE_STARTED);
                } else {
                    mRoot.changeLockScreenState(LockScreenRootView.STATE_CREATED);
                }
            }
        }
    }

    private Intent getCameraIntent() {
        PackageManager packageManager = mContext.getPackageManager();
        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        boolean currentUserHasTrust = updateMonitor.getUserHasTrust(
                mLockPatternUtils.getCurrentUser());
        boolean needSecureIntent = mLockPatternUtils.isSecure() && !currentUserHasTrust;

        if (needSecureIntent) {
            if (isActivityAvailable(SECURE_LEWA_CAMERA_INTENT) &&
                    !wouldLaunchResolverActivity(SECURE_LEWA_CAMERA_INTENT)) {
                return SECURE_LEWA_CAMERA_INTENT;
            }

            if (isActivityAvailable(SECURE_CAMERA_INTENT) &&
                    !wouldLaunchResolverActivity(SECURE_CAMERA_INTENT)) {
                return SECURE_CAMERA_INTENT;
            }
        } else {
            if (isActivityAvailable(INSECURE_LEWA_CAMERA_INTENT)) {
                return INSECURE_LEWA_CAMERA_INTENT;
            }
        }

        return INSECURE_CAMERA_INTENT;
    }

    private boolean isActivityAvailable(Intent intent) {
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> appList = packageManager.queryIntentActivitiesAsUser(
                intent, PackageManager.MATCH_DEFAULT_ONLY, mLockPatternUtils.getCurrentUser());
        return appList.size() > 0;
    }

    private boolean wouldLaunchResolverActivity(Intent intent) {
        PackageManager packageManager = mContext.getPackageManager();
        ResolveInfo resolved = packageManager.resolveActivityAsUser(intent,
                PackageManager.MATCH_DEFAULT_ONLY, mLockPatternUtils.getCurrentUser());
        List<ResolveInfo> appList = packageManager.queryIntentActivitiesAsUser(
                intent, PackageManager.MATCH_DEFAULT_ONLY, mLockPatternUtils.getCurrentUser());
        return wouldLaunchResolverActivity(resolved, appList);
    }

    private boolean wouldLaunchResolverActivity(ResolveInfo resolved, List<ResolveInfo> appList) {
        // If the list contains the above resolved activity, then it can't be
        // ResolverActivity itself.
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo tmp = appList.get(i);
            if (tmp.activityInfo.name.equals(resolved.activityInfo.name)
                    && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }
}
