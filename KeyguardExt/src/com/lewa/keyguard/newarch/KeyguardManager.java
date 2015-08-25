package com.lewa.keyguard.newarch;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by ning on 15-3-27.
 */
public interface KeyguardManager {
    public void userActivity();

    public void dismiss();

    public void launchActivity(Intent intent, boolean secure);

    public void launchActivity(Intent intent, Bundle animation, boolean secure);

    public void launchCamera();

    public void setNeedsInput(boolean needsInput);

    public boolean isSecure();
}
