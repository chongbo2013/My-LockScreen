package com.lewa.keyguard;

import android.content.Context;

/**
 * Translate the system's Keyguard event to lewa LockScreen event.
 */
public class LockScreenStateTranslator {
    private Context mContext;
    private LockScreen mLockScreen;

    private boolean mIsAttached = false;
    private boolean mIsWaitingShow = true;
    private boolean mIsScreenOn = true;
    private boolean mIsFadingAway = false;

    /**
     * Attach the lock screen to the keyguard at very first.
     */
    public LockScreenStateTranslator(Context context, LockScreen lockScreen) {
        mContext = context;
        mLockScreen = lockScreen;
    }

    /**
     * Show the LockScreen.
     */
    public void onScreenTurnedOn() {
        mIsScreenOn = true;
        mIsWaitingShow = false;
        if (mIsAttached) {
            mLockScreen.showKeyguard();
        }
    }

    /**
     * The screen may turn off but the Keyguard need not show.
     */
    public void onScreenTurnedOff() {
        mIsScreenOn = false;
        mIsWaitingShow = true;
    }

    /**
     * The showKeyguard event when the prev-event is screen off;
     */
    public void showKeyguard() {
        if (mIsWaitingShow) {
            mIsWaitingShow = false;
            if (mIsAttached) { // Turn off when the lockscreen is showing
                try {
                    Thread.sleep(100, 0); // hiding slowly
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mLockScreen.hideKeyguard();
            } else { // Turn off when the keyguard is dismissed
                mIsAttached = true;
                mLockScreen.onAttachToKeyguard(mContext);
            }
        } else if (mIsScreenOn) { // Called when the screen is turn on. [boot]
            if (!mIsAttached) {
                mLockScreen.onAttachToKeyguard(mContext);
            }
            mLockScreen.showKeyguard();
        }
    }

    /**
     * This method is only triggered when the keyguard is totally dismissed
     */
    public void hideKeyguard() {
        mIsWaitingShow = false;
        if (mIsAttached) {
            mLockScreen.hideKeyguard();
            if (mIsFadingAway) { // Keyguard is leaving
                mIsAttached = false;
                mLockScreen.onDetachFromKeyguard();
            }
        } else {
            mIsAttached = true;
            mLockScreen.onAttachToKeyguard(mContext);
            mLockScreen.hideKeyguard();
        }
    }

    /**
     * Only triggered when the LockScreen is attached to the Keyguard.
     */
    public void onBouncerShow() {
        if (mIsAttached) {
            mLockScreen.onBouncerShow();
        }
    }

    /**
     * Only triggered when the LockScreen is attached to the Keyguard.
     */
    public void onBouncerHide() {
        if (mIsAttached) {
            mLockScreen.onBouncerHide();
        }
    }

    public void setKeyguardFadingAway(long delay, long fadeoutDuration) {
        mIsFadingAway = true;
    }

    public void finishKeyguardFadingAway() {
        mIsFadingAway = false;
    }
}
