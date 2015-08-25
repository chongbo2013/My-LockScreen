
package com.lewa.lockscreen.v5.lockscreen;

import org.w3c.dom.Element;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;

import com.lewa.lockscreen.laml.ScreenElementLoadException;
import com.lewa.lockscreen.laml.elements.AdvancedSlider;
import com.lewa.lockscreen.laml.util.Utils;

public class UnlockerScreenElement extends AdvancedSlider {

    private static final String LOG_TAG = "LockScreen_UnlockerScreenElement";

    public static final String TAG_NAME = "Unlocker";

    private boolean mAlwaysShow;

    private int mDelay;

    private boolean mNoUnlock;

    private boolean mUnlockingHide;

    public UnlockerScreenElement(Element node, LockScreenRoot root)
            throws ScreenElementLoadException {
        super(node, root);
        mAlwaysShow = Boolean.parseBoolean(node.getAttribute("alwaysShow"));
        mNoUnlock = Boolean.parseBoolean(node.getAttribute("noUnlock"));
        mDelay = Utils.getAttrAsInt(node, "delay", 0);
    }

    private LockScreenRoot getRoot() {
        return (LockScreenRoot) mRoot;
    }

    public void endUnlockMoving(UnlockerScreenElement ele) {
        if (ele != this && !mAlwaysShow)
            mUnlockingHide = false;
    }

    public void finish() {
        super.finish();
        mUnlockingHide = false;
    }

    public boolean isVisible() {
        return super.isVisible() && !mUnlockingHide;
    }

    protected void onCancel() {
        super.onCancel();
        getRoot().endUnlockMoving(this);
    }

    protected boolean onLaunch(String name, Intent intent) {
        super.onLaunch(name, intent);
        if (mNoUnlock && intent == null) {
            getRoot().pokeWakelock();
            return false;
        }
        getRoot().endUnlockMoving(this);
        try {
            getRoot().unlocked(intent, mDelay);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return true;
    }

    protected void onStart() {
        super.onStart();
        getRoot().startUnlockMoving(this);
        getRoot().pokeWakelock();
    }

    public void startUnlockMoving(UnlockerScreenElement ele) {
        if (ele != this && !mAlwaysShow) {
            mUnlockingHide = true;
            resetInner();
        }
    }
}
