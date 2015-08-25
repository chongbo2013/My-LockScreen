
package com.lewa.lockscreen.v5.lockscreen.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.lewa.lockscreen.v5.lockscreen.LockScreenResourceLoader;

public class LockScreenActivity extends Activity {
    private static final String TAG = "LockScreenActivity";

    private static final boolean IS_MEIZU = Build.MANUFACTURER.equalsIgnoreCase("meizu");

    public static boolean sLocked = false;

    private FancyLockScreen mLockScreen;

    private boolean mClearCache = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (IS_MEIZU)
            hideBottomActionBar();
        sLocked = true;
        super.onCreate(savedInstanceState);
        setContentView(mLockScreen = new FancyLockScreen(this));
        initStrictMode();
    }

    private static final boolean DEVELOPER_MODE = false ;
    private void initStrictMode(){
        if (DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().penaltyLog() .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().penaltyLog() .build());
        }
    }
  
    public void unlock() {
        sLocked = false;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLockScreen.cleanUp();
        if (mClearCache)
            FancyLockScreen.clearCache();
    }

    public void killProcess(){
        if(LockScreenResourceLoader.DEBUG_LOCKSTYLE){
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLockScreen.onResume();
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!sLocked)
            mLockScreen.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*
         * if(keyCode == KeyEvent.KEYCODE_BACK) return true;
         */
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private void hideBottomActionBar() {
        int resId = getResources().getIdentifier("split_action_bar", "id", "android");
        if (resId != 0) {
            View v = getWindow().getDecorView().findViewById(resId);
            if (v != null && v.getVisibility() != View.GONE)
                v.setVisibility(View.GONE);
            Log.d(TAG, "hideBottomActionBar");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Clear & Quit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                mClearCache = true;
                finish();
                break;

            default:
                break;
        }
        return true;
    }
}
