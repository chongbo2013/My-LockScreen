
package com.lewa.lockscreen.laml.data;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;

import com.lewa.lockscreen.laml.util.IndexedNumberVariable;

public class VolumeVariableUpdater extends NotifierVariableUpdater {

    private static final int SHOW_DELAY_TIME = 1000;

    public static final String VAR_VOLUME_LEVEL = "volume_level";

    public static final String VAR_VOLUME_LEVEL_OLD = "volume_level_old";

    public static final String VAR_VOLUME_TYPE = "volume_type";

    private IndexedNumberVariable mVolumeLevel;

    private IndexedNumberVariable mVolumeLevelOld;

    private IndexedNumberVariable mVolumeType;

    private Handler mHandler = new Handler();

    private final Runnable mResetType = new Runnable() {
        public void run() {
            mVolumeType.set(-1);
        }
    };

    public VolumeVariableUpdater(VariableUpdaterManager m) {
        super(m, AudioManager.VOLUME_CHANGED_ACTION);
        mVolumeLevel = new IndexedNumberVariable(VAR_VOLUME_LEVEL, getContext().mVariables);
        mVolumeLevelOld = new IndexedNumberVariable(VAR_VOLUME_LEVEL_OLD, getContext().mVariables);
        mVolumeType = new IndexedNumberVariable(VAR_VOLUME_TYPE, getContext().mVariables);
        mVolumeType.set(-1);
        AudioManager am = (AudioManager) m.getContext().getContext().getSystemService(Context.AUDIO_SERVICE);
        mVolumeLevel.set(am.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    public void onNotify(Context context, Intent intent, Object o) {
        if (intent.getAction().equals(AudioManager.VOLUME_CHANGED_ACTION)) {
            int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
            mVolumeType.set(streamType);
            int newVolLevel = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, 0);
            mVolumeLevel.set(newVolLevel);
            int oldVolLevel = intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, 0);
            if (oldVolLevel != newVolLevel)
                mVolumeLevelOld.set(oldVolLevel);
            getContext().requestUpdate();
            mHandler.removeCallbacks(mResetType);
            mHandler.postDelayed(mResetType, SHOW_DELAY_TIME);
        }
    }

}
