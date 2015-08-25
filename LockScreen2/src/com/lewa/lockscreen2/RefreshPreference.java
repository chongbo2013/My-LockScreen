package com.lewa.lockscreen2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.lewa.lockscreen2.net.HttpRequest;
import com.lewa.lockscreen2.util.Constant;
import com.lewa.lockscreen2.util.LogUtil;
import com.lewa.lockscreen2.util.SharedPreferencesUtil;

/**
 * Created by lewa on 3/20/15.
 */
public class RefreshPreference extends CheckBoxPreference implements View.OnClickListener {

    private ImageView mImgRefresh;
    private ProgressBar mBar;
    private RefreshComplete mRefreshComplete;

    public RefreshPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.refresh_preference);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.REFRESH_DATA_COMPLETE_ACTION);
        mRefreshComplete = new RefreshComplete();
        context.registerReceiver(mRefreshComplete, filter);
    }

    public RefreshPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.checkBoxPreferenceStyle);
    }

    public RefreshPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        LogUtil.d("onBindView() -------------->");
        mBar = (ProgressBar) view.findViewById(R.id.bar);
        mImgRefresh = (ImageView) view.findViewById(R.id.img_refresh);
        mImgRefresh.setEnabled(true);
        mImgRefresh.setClickable(true);
        mImgRefresh.setOnClickListener(this);
        mBar.setOnClickListener(this);
    }

    public void onDestroyView() {
        LogUtil.d("onDestroyView() -------------->");
        getContext().unregisterReceiver(mRefreshComplete);
    }

    @Override
    public void onClick(View view) {
        LogUtil.d("onClick ---------------> ");
        switch (view.getId()) {
            case R.id.img_refresh:
                if (!isAvailable()) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.network_toast_prompt), Toast.LENGTH_SHORT).show();
                    return;
                }
                getContext().sendBroadcast(new Intent(Constant.REFRESH_DATA_ACTION));
                mBar.setVisibility(View.VISIBLE);
                mImgRefresh.setVisibility(View.GONE);
                break;
            case R.id.bar:
                //
                break;
        }
    }

    @Override
    protected void onPrepareForRemoval() {
        LogUtil.d("onPrepareForRemoval ---------------> ");
        super.onPrepareForRemoval();
    }

    private class RefreshComplete extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.REFRESH_DATA_COMPLETE_ACTION.equals(intent.getAction())) {
                mBar.setVisibility(View.GONE);
                mImgRefresh.setVisibility(View.VISIBLE);
                int wallpaperCount = intent.getIntExtra("wallpaperCount", 0);
                String toastText;
                if (wallpaperCount == 0) {
                    toastText = getContext().getResources().getString(R.string.toast_update_lastest);
                } else if (wallpaperCount == 1) {
                    toastText = String.format(getContext().getResources().getString(R.string.toast_update_success), wallpaperCount);
                } else if (wallpaperCount > 1) {
                    toastText = String.format(getContext().getResources().getString(R.string.toast_update_success2), wallpaperCount);
                } else {
                    toastText = getContext().getResources().getString(R.string.toast_update_fail);
                }
                LogUtil.d("toastText:" + toastText);
                if (isAvailable()) {
                    Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public boolean isAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            return cm.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
}
