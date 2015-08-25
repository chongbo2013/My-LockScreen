package com.lewa.lockscreen2;

import android.os.Bundle;
import com.lewa.keyguard.newarch.LockScreenBaseActivity;

/**
 * Created by ning on 15-4-2.
 */
public class LockScreenActivity extends LockScreenBaseActivity {
    LockscreenLayout mLockScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);
        mLockScreen = (LockscreenLayout) findViewById(R.id.rl_lockscreen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLockScreen.onAttachToKeyguard(getKeyguardManager());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLockScreen.showKeyguard();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLockScreen.hideKeyguard();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLockScreen.onDetachFromKeyguard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLockScreen.cleanUp();
    }

    @Override
    protected void onBouncerOpened() {
        super.onBouncerOpened();
        mLockScreen.onBouncerShow();
    }

    @Override
    protected void onBouncerClosed() {
        super.onBouncerClosed();
        mLockScreen.onBouncerHide();
    }
}
