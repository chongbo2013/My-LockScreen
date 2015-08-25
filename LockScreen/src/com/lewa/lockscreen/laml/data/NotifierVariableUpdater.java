
package com.lewa.lockscreen.laml.data;

import android.content.Context;
import android.content.Intent;

import com.lewa.lockscreen.laml.NotifierManager;
import com.lewa.lockscreen.laml.NotifierManager.OnNotifyListener;

public abstract class NotifierVariableUpdater extends VariableUpdater implements OnNotifyListener {

    protected NotifierManager mNotifierManager;

    private String mType;

    public NotifierVariableUpdater(VariableUpdaterManager m, String type) {
        super(m);
        mType = type;
        mNotifierManager = NotifierManager.getInstance(m.getContext().getContext());
    }

    public void finish() {
        mNotifierManager.releaseNotifier(mType, this);
    }

    public void init() {
        mNotifierManager.acquireNotifier(mType, this);
    }

    public abstract void onNotify(Context context, Intent intent, Object obj);

    public void pause() {
        mNotifierManager.pause(mType, this);
    }

    public void resume() {
        mNotifierManager.resume(mType, this);
    }
}
