package com.lewa.keyguard.newarch;

import android.content.*;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;

/**
 * Created by ning on 15-4-1.
 */
public class CombineContext extends ContextWrapper {
    private Context mResContext;
    private Context mAppContext;

    private LayoutInflater mInflater;

    public CombineContext(Context resContext, Context appContext) {
        super(appContext);
        mResContext = resContext;
        mAppContext = appContext;
    }

    @Override
    public String getPackageName() {
        return mResContext.getPackageName();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return mResContext.getSharedPreferences(name, mode);
    }

    @Override
    public AssetManager getAssets() {
        return mResContext.getAssets();
    }

    @Override
    public Resources getResources() {
        return mResContext.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        return mResContext.getClassLoader();
    }

    @Override
    public Resources.Theme getTheme() {
        return mResContext.getTheme();
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(mAppContext).cloneInContext(this);
            }
            return mInflater;
        }
        return mAppContext.getSystemService(name);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return mAppContext.registerReceiver(receiver, filter);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return mAppContext.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        mAppContext.unregisterReceiver(receiver);
    }
}
