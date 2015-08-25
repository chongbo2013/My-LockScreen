package com.lewa.keyguard.newarch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by ning on 15-3-27.
 */
public class LockScreenRootView extends FrameLayout implements LockScreenManager.OnRootViewChangeListener {
    public static final int STATE_CREATED = 0;
    public static final int STATE_STARTED = 1;
    public static final int STATE_RUNNING = 2;

    private View mRoot;

    private final KeyguardManager mKeyguardManager;
    private final LockScreenManager mLockScreenManager;
    private final LockScreenManager.LockScreenController mLockScreenController;

    public LockScreenRootView(Context context, KeyguardManager keyguardManager) {
        super(context);
        mKeyguardManager = keyguardManager;
        mLockScreenManager = new LockScreenManager(context, keyguardManager);
        mLockScreenController = mLockScreenManager.getLockScreenController();
        mLockScreenController.setOnRootViewChangeListener(this);
        onRootViewChanged(mLockScreenController.getRootView());
    }

    public void changeLockScreenState(int state) {
        mLockScreenController.gotoState(state);
    }

    public void onBouncerOpened() {
        mLockScreenController.onBouncerOpened();
    }

    public void onBouncerClosed() {
        mLockScreenController.onBouncerClosed();
    }

    @Override
    public void onRootViewChanged(View view) {
        if (view == null || view == mRoot) {
            return;
        }

        if (mRoot != null) {
            removeView(mRoot);
        }

        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRoot = view;
    }
}
