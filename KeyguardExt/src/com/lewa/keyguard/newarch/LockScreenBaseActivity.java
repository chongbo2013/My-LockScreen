package com.lewa.keyguard.newarch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by ning on 15-3-31.
 */
public class LockScreenBaseActivity extends Activity {
    private static final String TAG = "LockScreenBaseActivity";

    private boolean mIsAttachFromKeyguard = false;
    private Context mResContext;
    private Context mAppContext;
    private Context mCombineContext;
    private KeyguardManager mKeyguardManager;

    private ViewGroup mRootView;


    final void attach(Context resContext, Context appContext, Object keyguardManagerObj) {
        mIsAttachFromKeyguard = true;

        mResContext = resContext;
        mAppContext = appContext;
        mCombineContext = new CombineContext(resContext, appContext);

        attachBaseContext(mCombineContext);

        mKeyguardManager = (KeyguardManager) (InterfaceCast.cast(KeyguardManager.class, keyguardManagerObj));
    }

    final void performCreate() {
        onCreate(null);
    }

    final void performStart() {
        onStart();
    }

    final void performResume() {
        onResume();
    }

    final void performPause() {
        onPause();
    }

    final void performStop() {
        onStop();
    }

    final void performDestroy() {
        onDestroy();
    }

    final ViewGroup getRootView() {
        if (mRootView == null) {
            mRootView = new FrameLayout(mAppContext);
            mRootView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mRootView;
    }

    final protected KeyguardManager getKeyguardManager() {
        return mKeyguardManager;
    }

    protected void onBouncerOpened() {
        Log.d(TAG, "[" + this + "]onBouncerOpened");
    }

    protected void onBouncerClosed() {
        Log.d(TAG, "[" + this + "]onBouncerClosed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[" + this + "]onCreate");

        if (!mIsAttachFromKeyguard) {
            super.onCreate(savedInstanceState);
            return;
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "[" + this + "]onStart");

        if (!mIsAttachFromKeyguard) {
            super.onStart();
            return;
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "[" + this + "]onResume");

        if (!mIsAttachFromKeyguard) {
            super.onResume();
            return;
        }

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "[" + this + "]onPause");

        if (!mIsAttachFromKeyguard) {
            super.onPause();
            return;
        }

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "[" + this + "]onStop");

        if (!mIsAttachFromKeyguard) {
            super.onStop();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "[" + this + "]onDestroy");

        if (!mIsAttachFromKeyguard) {
            super.onDestroy();
            return;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if (!mIsAttachFromKeyguard) {
            super.setContentView(layoutResID);
            return;
        }

//        LayoutInflater appInflater = (LayoutInflater)
//                mAppContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        appInflater = appInflater.cloneInContext(mResContext);
        LayoutInflater appInflater = LayoutInflater.from(mCombineContext);
        View view = appInflater.inflate(layoutResID, null, false);
        getRootView().addView(view);
    }

    @Override
    public void setContentView(View view) {
        if (!mIsAttachFromKeyguard) {
            super.setContentView(view);
            return;
        }

        getRootView().addView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (!mIsAttachFromKeyguard) {
            super.setContentView(view, params);
            return;
        }

        getRootView().addView(view, params);
    }

    @Override
    public View findViewById(int id) {
        if (!mIsAttachFromKeyguard) {
            return super.findViewById(id);
        }

        return getRootView().findViewById(id);
    }

    @Override
    public Object getSystemService(String name) {
        if (!mIsAttachFromKeyguard) {
            return super.getSystemService(name);
        }

        return mCombineContext.getSystemService(name);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (!mIsAttachFromKeyguard) {
            return super.registerReceiver(receiver, filter);
        }

        return mCombineContext.registerReceiver(receiver, filter);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (!mIsAttachFromKeyguard) {
            return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
        }

        return mCombineContext.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (!mIsAttachFromKeyguard) {
            super.unregisterReceiver(receiver);
        }

        mCombineContext.unregisterReceiver(receiver);
    }
}
