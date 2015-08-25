package com.lewa.keyguard;

import android.content.Intent;
import android.os.Bundle;

public class KeyguardManagerBinder extends KeyguardManager {
    private static KeyguardManagerBinder mInstance;

    private KeyguardManager mImpl;

    private KeyguardManagerBinder() {

    }

    static KeyguardManagerBinder instance() {
        if (mInstance == null) {
            mInstance = new KeyguardManagerBinder();
        }
        return mInstance;
    }

    public void bind(KeyguardManager impl) {
        mImpl = impl;
    }

    public void unbind() {
        mImpl = null;
    }

    @Override
    public void dismiss() {
        if (mImpl != null) {
            mImpl.dismiss();
        }
    }

    @Override
    public void userActivity() {
        if (mImpl != null) {
            mImpl.userActivity();
        }
    }

    @Override
    public void launchActivity(Intent intent, boolean secure) {
        if (mImpl != null) {
            mImpl.launchActivity(intent, secure);
        }
    }

    @Override
    public void launchActivity(Intent intent, Bundle animation, boolean secure) {
        if (mImpl != null) {
            mImpl.launchActivity(intent, animation, secure);
        }
    }

    @Override
    public void launchCamera() {
        if (mImpl != null) {
            mImpl.launchCamera();
        }
    }
}
