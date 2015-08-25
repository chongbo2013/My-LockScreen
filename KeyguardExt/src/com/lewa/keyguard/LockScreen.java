package com.lewa.keyguard;

import android.content.Context;

public interface LockScreen {
    public void onAttachToKeyguard(Context context);

    public void onDetachFromKeyguard();

    public void showKeyguard();

    public void hideKeyguard();

    public void onBouncerShow();

    public void onBouncerHide();

    public void cleanUp();
}
