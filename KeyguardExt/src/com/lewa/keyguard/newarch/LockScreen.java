package com.lewa.keyguard.newarch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ning on 15-3-31.
 */
interface LockScreen {
    public void attach(Context resContext, Context appContext, Object keyguardManagerObj);

    public void performCreate();

    public void performStart();

    public void performResume();

    public void performPause();

    public void performStop();

    public void performDestroy();

    public void onBouncerOpened();

    public void onBouncerClosed();

    public ViewGroup getRootView();
}
