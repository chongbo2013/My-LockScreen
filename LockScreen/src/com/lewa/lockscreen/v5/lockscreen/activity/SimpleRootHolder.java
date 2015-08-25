
package com.lewa.lockscreen.v5.lockscreen.activity;

import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.lewa.lockscreen.content.res.ThemeConstants;
import com.lewa.lockscreen.laml.LifecycleResourceManager;
import com.lewa.lockscreen.laml.RenderThread;
import com.lewa.lockscreen.laml.ScreenContext;
import com.lewa.lockscreen.v5.lockscreen.FancyLockScreenView;
import com.lewa.lockscreen.v5.lockscreen.LockScreenElementFactory;
import com.lewa.lockscreen.v5.lockscreen.LockScreenResourceLoader;
import com.lewa.lockscreen.v5.lockscreen.LockScreenRoot;

public class SimpleRootHolder {

    private static final String LOG_TAG = "SimpleRootHolder";

    private ScreenContext mContext;

    private LifecycleResourceManager mResourceMgr;

    private LockScreenRoot mRoot;

    private RenderThread mThread;

    private Stack<FancyLockScreen> mViewList = new Stack<FancyLockScreen>();

    public void cleanUp(FancyLockScreen ls) {
        mViewList.remove(ls);
        ls.cleanUpView();
        Log.d(LOG_TAG, "cleanUp: " + ls.toString() + " size:" + mViewList.size());
        if (mViewList.size() == 0) {
            mRoot.getContext().mVariables.reset();
            Log.d(LOG_TAG, "cleanUp finish");
        } else {
            mViewList.peek().rebindView();
            mRoot.init();
        }
    }

    public void clear() {
        mRoot = null;
        mContext = null;
        if (mResourceMgr != null) {
            mResourceMgr.clear();
            mResourceMgr = null;
        }
        if (mThread != null) {
            mThread.setStop();
            mThread = null;
        }
    }

    public FancyLockScreenView createView(Context context) {
        FancyLockScreenView view = new FancyLockScreenView(context, mRoot, mThread);
        Log.d(LOG_TAG, "createView");
        return view;
    }

    public ScreenContext getContext() {
        return mContext;
    }

    public LockScreenRoot getRoot() {
        return mRoot;
    }

    public RenderThread getThread() {
        return mThread;
    }

    public void init(Context context, FancyLockScreen ls) {
        if (mRoot == null) {
            LockScreenResourceLoader res = new LockScreenResourceLoader();
            res.setLocal(context.getResources().getConfiguration().locale);
            mResourceMgr = new LifecycleResourceManager(res, LifecycleResourceManager.TIME_DAY, LifecycleResourceManager.TIME_HOUR);
            mContext = new ScreenContext(context, mResourceMgr, new LockScreenElementFactory());
            mContext.mRoot = mRoot = new LockScreenRoot(mContext);
            mRoot.setConfig(ThemeConstants.CONFIG_EXTRA_PATH);
            mRoot.load();
            mThread = new RenderThread();
            mThread.start();
            Log.d(LOG_TAG, "create root");
        }
        mViewList.push(ls);
        Log.d(LOG_TAG, "init:" + ls);
    }
 
    public Bitmap getBitmap(String name){
        if( mResourceMgr != null && !TextUtils.isEmpty(name)){
            return mResourceMgr.getBitmap(name);
        }
        return null;
    }
}
