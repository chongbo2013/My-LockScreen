package com.lewa.lockscreen2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.lewa.lockscreen2.util.Constant;
import com.lewa.lockscreen2.util.LogUtil;
import com.lewa.lockscreen2.util.SharedPreferencesUtil;

/**
 * Created by lewa on 4/22/15.
 */
public class LockscreenSettingsFragment extends PreferenceFragment {

    private Preference mShortcutPreference;
    private Preference mRotationPreference;
    private Preference mAutoUpdatePreference;
    private String[] rotations;
    private String[] autoUpdate;

    public LockscreenSettingsFragment() {

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("onCreate() -------------->");
        addPreferencesFromResource(R.xml.lockscreen_settings);
        setupView();
    }

    @Override
    public void onDestroyView() {
        LogUtil.d("onDestroyView() -------------->");
        if (mAutoUpdatePreference instanceof RefreshPreference){
            ((RefreshPreference) mAutoUpdatePreference).onDestroyView();
        }
        super.onDestroyView();
    }

    private void setupView() {
        mShortcutPreference = (Preference) super.findPreference("shortcut_manager");
        mRotationPreference = (Preference) super.findPreference("wallpaper_rotation");
        mAutoUpdatePreference = (Preference) super.findPreference("auto_update");
        rotations = getResources().getStringArray(R.array.wallpaper_rotation);
        autoUpdate = getResources().getStringArray(R.array.auto_update);

        int rotationsIndex = SharedPreferencesUtil.getSettingInt(getActivity(), Constant.WALLPAPER_ROTATION_TYPE, Constant.WallpaperRotation.SCREEN_ON.ordinal());
        int autoUpdateIndex = SharedPreferencesUtil.getSettingInt(getActivity(), Constant.NETWORK_DOWNLOAD_TYPE, Constant.DownloadType.WIFI_UPDATE.ordinal());
        mRotationPreference.setSummary(rotations[rotationsIndex]);
        mAutoUpdatePreference.setSummary(autoUpdate[autoUpdateIndex]);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mShortcutPreference == preference) {
            Intent intent = new Intent(getActivity(), LockscreenAppManager.class);
            startActivity(intent);
        } else if (mRotationPreference == preference) {
            showRotation();
        } else if (mAutoUpdatePreference == preference) {
            showAutoUpdate();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showRotation() {
        int checkedItem = SharedPreferencesUtil.getSettingInt(getActivity(), Constant.WALLPAPER_ROTATION_TYPE, Constant.WallpaperRotation.SCREEN_ON.ordinal());
        LogUtil.d("showRotation -----------------> checkedItem:" + checkedItem);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.wallpaper_rotation));
        builder.setSingleChoiceItems(rotations, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // save rotation
                LogUtil.d("showRotation -----------------> " + i);
                SharedPreferencesUtil.setSetting(getActivity(), Constant.WALLPAPER_ROTATION_TYPE, i);
                mRotationPreference.setSummary(rotations[i]);
                Intent intent = new Intent(Constant.WALLPAPER_ROTATION_TYPE_ACTION);
                intent.putExtra("rotation_type_key", i);
                getActivity().sendBroadcast(intent);
                dialogInterface.cancel();
            }
        }).show();
    }

    private void showAutoUpdate() {
        int checkedItem = SharedPreferencesUtil.getSettingInt(getActivity(), Constant.NETWORK_DOWNLOAD_TYPE, Constant.DownloadType.WIFI_UPDATE.ordinal());
        LogUtil.d("showAutoUpdate -----------------> checkedItem:" + checkedItem);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.auto_update));
        builder.setSingleChoiceItems(autoUpdate, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // save autoUpdate
                        LogUtil.d("showAutoUpdate -----------------> " + i);
                        SharedPreferencesUtil.setSetting(getActivity(), Constant.NETWORK_DOWNLOAD_TYPE, i);
                        mAutoUpdatePreference.setSummary(autoUpdate[i]);
                        Intent intent = new Intent(Constant.NETWORK_DOWNLOAD_TYPE_ACTION);
                        intent.putExtra("download_type_key", i);
                        getActivity().sendBroadcast(intent);
                        dialogInterface.cancel();
                    }
                }).show();
    }
}