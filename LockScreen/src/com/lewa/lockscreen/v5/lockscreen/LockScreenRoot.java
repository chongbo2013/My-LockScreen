
package com.lewa.lockscreen.v5.lockscreen;

import org.w3c.dom.Element;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;

import com.lewa.lockscreen.laml.Constants;
import com.lewa.lockscreen.laml.ScreenContext;
import com.lewa.lockscreen.laml.ScreenElementRoot;
import com.lewa.lockscreen.laml.ScreenElementRoot.OnExternCommandListener;
import com.lewa.lockscreen.laml.data.BatteryVariableUpdater;
import com.lewa.lockscreen.laml.data.VariableNames;
import com.lewa.lockscreen.laml.data.VariableUpdaterManager;
import com.lewa.lockscreen.laml.data.VolumeVariableUpdater;
import com.lewa.lockscreen.laml.elements.ButtonScreenElement;
import com.lewa.lockscreen.laml.elements.ElementGroup;
import com.lewa.lockscreen.laml.elements.ScreenElement;
import com.lewa.lockscreen.laml.util.IndexedNumberVariable;
import com.lewa.lockscreen.laml.util.Task;
import com.lewa.lockscreen.laml.util.Utils;
import static com.lewa.lockscreen.laml.data.BatteryVariableUpdater.*;

public class LockScreenRoot extends ScreenElementRoot implements OnExternCommandListener {

    private static final String LOG_TAG = "LockScreenRoot";

    public static final String SMS_BODY_PREVIEW = "sms_body_preview";

    private String curCategory;

    private BatteryInfo mBatteryInfo;

    private IndexedNumberVariable mBatteryLevel;

    private IndexedNumberVariable mBatteryState;

    private boolean mDisplayDesktop;

    private float mFrameRateBatteryFull;

    private float mFrameRateBatteryLow;

    private float mFrameRateCharging;

    private boolean mInit;

    private UnlockerCallback mUnlockerCallback;

    public LockScreenRoot(ScreenContext c) {
        super(c);
        mBatteryState = new IndexedNumberVariable(VariableNames.BATTERY_STATE, mContext.mVariables);
        mBatteryLevel = new IndexedNumberVariable(VariableNames.BATTERY_LEVEL, mContext.mVariables);
        setOnExternCommandListener(this);
    }

    private void endUnlockMoving(ElementGroup g, UnlockerScreenElement ele) {
        if (g != null) {
            for (ScreenElement e : g.getElements()) {
                if (e instanceof ElementGroup) {
                    endUnlockMoving((ElementGroup) e, ele);
                } else if (e instanceof UnlockerScreenElement) {
                    ((UnlockerScreenElement) e).endUnlockMoving(ele);
                }
            }
        }
    }

    private void startUnlockMoving(ElementGroup g, UnlockerScreenElement ele) {
        if (g != null) {
            for (ScreenElement e : g.getElements()) {
                if (e instanceof ElementGroup) {
                    startUnlockMoving((ElementGroup) e, ele);
                } else if (e instanceof UnlockerScreenElement) {
                    ((UnlockerScreenElement) e).startUnlockMoving(ele);
                }
            }
        }
    }

    public void endUnlockMoving(UnlockerScreenElement ele) {
        endUnlockMoving(mElementGroup, ele);
    }

    public Task findTask(String id) {
        return mUnlockerCallback.findTask(id);
    }

    public void finish() {
        super.finish();
        curCategory = null;
        mInit = false;
        mBatteryInfo = null;
    }

    public void haptic(int effectId) {
        mUnlockerCallback.haptic(effectId);
    }

    public void init() {
        super.init();
        boolean showSmsBodySetting = Settings.System.getInt(mContext.getContext().getContentResolver(),
                "pref_key_enable_notification_body", 1) == 1 && !mUnlockerCallback.isSecure();
        IndexedNumberVariable showSms = new IndexedNumberVariable(SMS_BODY_PREVIEW,
                mContext.mVariables);
        showSms.set(showSmsBodySetting ? 1 : 0);
        mInit = true;
        if (mBatteryInfo != null) {
            onRefreshBatteryInfo(mBatteryInfo.showBatteryInfo, mBatteryInfo.pluggedIn,
                    mBatteryInfo.batteryLevel);
            mBatteryInfo = null;
        }
    }

    public boolean isDisplayDesktop() {
        return mDisplayDesktop;
    }

    protected void onAddVariableUpdater(VariableUpdaterManager m) {
        super.onAddVariableUpdater(m);
        m.add(new BatteryVariableUpdater(m, mContext));
        m.add(new VolumeVariableUpdater(m));
    }

    public void onButtonInteractive(ButtonScreenElement e, ButtonScreenElement.ButtonAction a) {
        mUnlockerCallback.pokeWakelock();
    }

    protected boolean onLoad(Element root) {
        if (!super.onLoad(root)) {
            return false;
        } else {
            mDisplayDesktop = Boolean.parseBoolean(root.getAttribute("displayDesktop"));
            mFrameRateCharging = Utils.getAttrAsFloat(root, "frameRateCharging", mNormalFrameRate);
            mFrameRateBatteryLow = Utils.getAttrAsFloat(root, "frameRateBatteryLow",
                    mNormalFrameRate);
            mFrameRateBatteryFull = Utils.getAttrAsFloat(root, "frameRateBatteryFull",
                    mNormalFrameRate);
            BuiltinVariableBinders.fill(mVariableBinderManager);
            return true;
        }
    }

    public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel) {
        if (!mInit) {
            mBatteryInfo = new BatteryInfo(showBatteryInfo, pluggedIn, batteryLevel);
        } else {
            mBatteryLevel.set(batteryLevel);
            if (mElementGroup != null) {
                String s;
                int i;
                if (showBatteryInfo) {
                    if (pluggedIn) {
                        if (batteryLevel >= 100) {
                            s = TAG_NAME_BATTERYFULL;
                            i = VariableNames.BATTERY_STATE_FULL;
                            mFrameRate = mFrameRateBatteryFull;
                        } else {
                            s = TAG_NAME_CHARGING;
                            i = VariableNames.BATTERY_STATE_CHARGING;
                            mFrameRate = mFrameRateCharging;
                        }
                    } else if (batteryLevel <= 10){
                        s = TAG_NAME_LOWBATTERY;
                        i = VariableNames.BATTERY_STATE_LOW;
                        mFrameRate = mFrameRateBatteryLow;
                    } else {
                        s = TAG_NAME_NORMAL;
                        i = VariableNames.BATTERY_STATE_UNPLUGGED;
                        mFrameRate = mNormalFrameRate;
                    }
                } else {
                    s = TAG_NAME_NORMAL;
                    i = VariableNames.BATTERY_STATE_UNPLUGGED;
                    mFrameRate = mNormalFrameRate;
                }
                if (s != curCategory) {
                    requestFramerate(mFrameRate);
                    requestUpdate();
                    mBatteryState.set(i);
                    mElementGroup.showCategory(TAG_NAME_BATTERYFULL, false);
                    mElementGroup.showCategory(TAG_NAME_CHARGING, false);
                    mElementGroup.showCategory(TAG_NAME_LOWBATTERY, false);
                    mElementGroup.showCategory(TAG_NAME_NORMAL, false);
                    mElementGroup.showCategory(curCategory = s, true);
                    Log.d(LOG_TAG, curCategory);
                }
            }
        }
    }

    public boolean onTouch(MotionEvent event) {
        if (mElementGroup == null) {
            mUnlockerCallback.unlocked(null, 0);
            return false;
        } else {
            return super.onTouch(event);
        }
    }

    public void pokeWakelock() {
        mUnlockerCallback.pokeWakelock();
    }

    public void setUnlockerCallback(UnlockerCallback unlockerCallback) {
        mUnlockerCallback = unlockerCallback;
    }

    protected boolean shouldPlaySound() {
        return mUnlockerCallback.isSoundEnable();
    }

    public void startUnlockMoving(UnlockerScreenElement ele) {
        startUnlockMoving(mElementGroup, ele);
    }

    public void unlocked(Intent intent, int delay) {
        mUnlockerCallback.unlocked(intent, delay);
    }

    public void unlocked(Intent intent, int delay,String enterResName ,String exitResName) {
        mUnlockerCallback.unlocked(intent, delay);
    }

    public  void unlocked(Intent intent,String enterResName ,String exitResName){
        unlocked(intent,0,enterResName,exitResName);
    }

    private static class BatteryInfo {

        public int batteryLevel;

        public boolean pluggedIn;

        public boolean showBatteryInfo;

        public BatteryInfo(boolean s, boolean p, int l) {
            showBatteryInfo = s;
            pluggedIn = p;
            batteryLevel = l;
        }
    }

    public static interface UnlockerCallback {

        public abstract Task findTask(String s);

        public abstract void haptic(int i);

        public abstract boolean isSecure();

        public abstract boolean isSoundEnable();

        public abstract void pokeWakelock();

        public abstract void pokeWakelock(int i);

        public abstract void unlocked(Intent intent, int i);

        public abstract void unlocked(Intent intent, int i,String enterResName ,String exitResName);

        public abstract String getProperty(String name);
    }

    @Override
    public void onCommand(String command, Double numPara, String strPara) {
        if ("unlock".equals(command)) {
            int delay = numPara == null ? 0 : numPara.intValue();
            Intent intent;
            if (strPara != null) {
                intent = new Intent(strPara);
                intent.setFlags(Constants.INTENT_FLAG);
                if (delay < 0) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    delay = 0;
                }
            } else {
                intent = null;
            }
            unlocked(intent, delay);
        }
    }
}
