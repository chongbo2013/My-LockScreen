
package com.lewa.lockscreen.v5.lockscreen;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.lewa.lockscreen.laml.AdvancedView;
import com.lewa.lockscreen.laml.RenderThread;

public class FancyLockScreenView extends AdvancedView {

    private static final String LOG_TAG = "FancyLockScreenView";

    private static final int MOVING_THRESHOLD = 25;

    private long mLastTouchTime;

    private int mPreX;

    private int mPreY;

    public FancyLockScreenView(Context context, LockScreenRoot root) {
        super(context, root);
        init();
    }

    public FancyLockScreenView(Context context, LockScreenRoot root, RenderThread t) {
        super(context, root, t);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        Log.d(LOG_TAG, "init");
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float scale = mRoot.getMatrixScale();
        int x = (int) (event.getX() / scale);
        int y = (int) (event.getY() / scale);
        if (action == MotionEvent.ACTION_MOVE) {
            if (SystemClock.elapsedRealtime() - mLastTouchTime >= 1000L) {
                int dx = x - mPreX;
                int dy = y - mPreY;
                if (dx * dx + dy * dy > MOVING_THRESHOLD) {
                    ((LockScreenRoot) mRoot).pokeWakelock();
                    mLastTouchTime = SystemClock.elapsedRealtime();
                    mPreX = x;
                    mPreY = y;
                }
            }
        } else if (action == MotionEvent.ACTION_DOWN) {
            mPreX = x;
            mPreY = y;
        }
        return super.onTouchEvent(event);
    }

    public void rebindRoot() {
        mRoot.setRenderController(mRendererController);
    }
}
