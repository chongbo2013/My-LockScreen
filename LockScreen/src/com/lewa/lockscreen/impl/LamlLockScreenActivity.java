package com.lewa.lockscreen.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lewa.keyguard.newarch.LockScreenBaseActivity;
import com.lewa.lockscreen.R;
import com.lewa.lockscreen.laml.ScreenElementRoot;

/**
 * Created by ning on 15-3-31.
 */
public class LamlLockScreenActivity extends LockScreenBaseActivity {
    public static final String COM_ANDROID_STARTFORLOCKSCREEN = "com.android.startforlockscreen";
    public static final String COM_ANDROID_STOPFORLOCKSCREEN = "com.android.stopforlockscreen";

    public static boolean IsFmEnabled = false;

    private LamlLockScreen2 mLamlLockScreen;
    private ImageView mBouncerBk;

    private BroadcastReceiver mFmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(COM_ANDROID_STARTFORLOCKSCREEN)) {
                ScreenElementRoot.IsFmEnabled = true;
            } else if (intent.getAction().equals(COM_ANDROID_STOPFORLOCKSCREEN)) {
                ScreenElementRoot.IsFmEnabled = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLamlLockScreen = new LamlLockScreen2(this);

        setContentView(R.layout.lockscreen);
        mBouncerBk = (ImageView) findViewById(R.id.bouncer_bk);
        mBouncerBk.setScaleType(ImageView.ScaleType.FIT_XY);
        ViewGroup laml = (ViewGroup) findViewById(R.id.laml);
        laml.addView(mLamlLockScreen);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(COM_ANDROID_STARTFORLOCKSCREEN);
        intentFilter.addAction(COM_ANDROID_STOPFORLOCKSCREEN);

        registerReceiver(mFmReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLamlLockScreen.onAttachToKeyguard(getKeyguardManager());
        mBouncerBk.setImageDrawable(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLamlLockScreen.showKeyguard();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLamlLockScreen.hideKeyguard();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLamlLockScreen.onDetachFromKeyguard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLamlLockScreen.cleanUp();
        unregisterReceiver(mFmReceiver);
    }

    @Override
    protected void onBouncerOpened() {
        super.onBouncerOpened();
        mLamlLockScreen.onBouncerShow();
        mBouncerBk.setImageDrawable(mLamlLockScreen.getDismissBackground());
    }

    @Override
    protected void onBouncerClosed() {
        super.onBouncerClosed();
        mLamlLockScreen.onBouncerHide();
        mBouncerBk.setImageDrawable(null);
    }
}
