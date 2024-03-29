/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.lewa.keyguard;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.android.internal.widget.LockPatternUtils;

import java.util.List;

/**
 * Utility class to inflate previews for phone and camera affordance.
 */
public class PreviewInflater {

    private static final String TAG = "PreviewInflater";

    private static final String META_DATA_KEYGUARD_LAYOUT = "com.android.keyguard.layout";

    private Context mContext;
    private LockPatternUtils mLockPatternUtils;

    public PreviewInflater(Context context, LockPatternUtils lockPatternUtils) {
        mContext = context;
        mLockPatternUtils = lockPatternUtils;
    }

    public View inflatePreview(Intent intent) {
        WidgetInfo info = getWidgetInfo(intent);
        if (info == null) {
            return null;
        }
        View v = inflateWidgetView(info);
        if (v == null) {
            return null;
        }
        KeyguardPreviewContainer container = new KeyguardPreviewContainer(mContext, null);
        container.addView(v);
        return container;
    }

    private View inflateWidgetView(WidgetInfo widgetInfo) {
        View widgetView = null;
        try {
            Context appContext = mContext.createPackageContext(
                    widgetInfo.contextPackage, Context.CONTEXT_RESTRICTED);
            LayoutInflater appInflater = (LayoutInflater)
                    appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            appInflater = appInflater.cloneInContext(appContext);
            widgetView = appInflater.inflate(widgetInfo.layoutId, null, false);
        } catch (PackageManager.NameNotFoundException|RuntimeException e) {
            Log.w(TAG, "Error creating widget view", e);
        }
        return widgetView;
    }

    private WidgetInfo getWidgetInfo(Intent intent) {
        WidgetInfo info = new WidgetInfo();
        PackageManager packageManager = mContext.getPackageManager();
        final List<ResolveInfo> appList = packageManager.queryIntentActivitiesAsUser(
                intent, PackageManager.MATCH_DEFAULT_ONLY, mLockPatternUtils.getCurrentUser());
        if (appList.size() == 0) {
            return null;
        }
        ResolveInfo resolved = packageManager.resolveActivityAsUser(intent,
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_META_DATA,
                mLockPatternUtils.getCurrentUser());
        if (wouldLaunchResolverActivity(resolved, appList)) {
            return null;
        }
        if (resolved == null || resolved.activityInfo == null) {
            return null;
        }
        if (resolved.activityInfo.metaData == null || resolved.activityInfo.metaData.isEmpty()) {
            return null;
        }
        int layoutId = resolved.activityInfo.metaData.getInt(META_DATA_KEYGUARD_LAYOUT);
        if (layoutId == 0) {
            return null;
        }
        info.contextPackage = resolved.activityInfo.packageName;
        info.layoutId = layoutId;
        return info;
    }

    public static boolean wouldLaunchResolverActivity(Context ctx, Intent intent,
            int currentUserId) {
        PackageManager packageManager = ctx.getPackageManager();
        final List<ResolveInfo> appList = packageManager.queryIntentActivitiesAsUser(
                intent, PackageManager.MATCH_DEFAULT_ONLY, currentUserId);
        if (appList.size() == 0) {
            return false;
        }
        ResolveInfo resolved = packageManager.resolveActivityAsUser(intent,
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_META_DATA, currentUserId);
        return wouldLaunchResolverActivity(resolved, appList);
    }

    private static boolean wouldLaunchResolverActivity(
            ResolveInfo resolved, List<ResolveInfo> appList) {
        // If the list contains the above resolved activity, then it can't be
        // ResolverActivity itself.
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo tmp = appList.get(i);
            if (tmp.activityInfo.name.equals(resolved.activityInfo.name)
                    && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }

    private static class WidgetInfo {
        String contextPackage;
        int layoutId;
    }
}
