
package com.lewa.lockscreen2;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.view.animation.BounceInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.*;
import com.lewa.lockscreen2.util.*;
import com.lewa.themes.ThemeManager;
import lewa.content.res.IconCustomizer;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lewa.support.v7.app.ActionBarActivity;
import lewa.support.v7.app.ActionBar;
import lewa.support.v7.view.ActionMode;
import android.provider.Settings;

public class LockscreenAppManager extends ActionBarActivity{

    private static final int MAX_CHECKED = 6;
    private static final float SCALE = 0.83f;
    static final String[] sP = {"com.android.calculator2", "com.android.deskclock", "com.android.soundrecorder"};
    static final List<String> sFilters = Arrays.asList(sP);
    private GridView mGirdViewApp;
    private AppAdapter mAppAdapter;
    private List<ResolveInfo> mResolves;
    private TimeInterpolator mTimeInterpolator;
    private PropertyValuesHolder[] mPvs;
    /**
     * <className, packageName></>
     */
    private HashMap<String, String> mCheckedMap = new HashMap<String, String>();
    private HashMap<String, Integer> mCheckIndexMap = new HashMap<String, Integer>();
    private static HashMap<String, Drawable> mCache = new HashMap<String, Drawable>();
    private PackageManager mPM;
    private static int mCheckedCount = 0;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("onCreate() --------------> ");
        ThemeManager.getInstance(LockscreenAppManager.this);
        mActionBar = this.getSupportActionBar();
        int flag = mActionBar.getDisplayOptions();
        mActionBar.setDisplayOptions(flag ^ ActionBar.DISPLAY_HOME_AS_UP ^ ActionBar.DISPLAY_SHOW_HOME);
        this.setContentView(R.layout.app_manager);
        setupView();
        init();
    }

    private void setupView() {
        mGirdViewApp = (GridView) super.findViewById(R.id.gv_appmanager);

        mActionBar.setTitle(String.format(getResources().getString(R.string.checked_prompt), MAX_CHECKED - mCheckedCount));
    }

    private void init() {
        LogUtil.d("init() --------------> ");
        mPM = this.getPackageManager();
        mTimeInterpolator = new BounceInterpolator();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mResolves = mPM.queryIntentActivities(intent, 0);

        initCheckedMap();
        mAppAdapter = new AppAdapter(mResolves);
        mGirdViewApp.setAdapter(mAppAdapter);
    }

    private void initCheckedMap() {
        String checkedMapStr = Settings.System.getString(getContentResolver(), Constant.CHECKED_APP_MAP);

        LogUtil.d("initCheckedMap() --------------> CHECKED_APP_MAP  checkedMapStr:" + checkedMapStr);
        if (checkedMapStr == null || "".equals(checkedMapStr)) {
            checkedMapStr = Constant.DEFAULT_APPMANAGER;
        }
        String[] appInfo = checkedMapStr.toString().split("&&");

        LogUtil.d("initCheckedMap() --------------> checkedMapStr:" + checkedMapStr);
        int length = appInfo.length;
        LogUtil.d("initCheckedMap ----------> " + length);
        int index = 0;
        for (int i = 0; i < length; i++) {
            String[] str = appInfo[i].split("#");
            if (str[0].length() > 0 && str.length > 2) {
                if (!isExistApk(str[1], str[0])) {
                    continue;
                }
                mCheckedMap.put(str[0], str[1]);
                mCheckIndexMap.put(str[0], /*Integer.parseInt(str[2])*/index);
                index++;
            }
        }
    }

    private boolean isExistApk(String packageName, String className) {
        for (ResolveInfo info : mResolves) {
            if (info.activityInfo.name.equals(className) && info.activityInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        LogUtil.d("onStart() --------------> ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogUtil.d("onResume() --------------> ");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        LogUtil.d("onRestart() --------------> ");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        LogUtil.d("onPause() --------------> ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.d("onStop() --------------> ");
        super.onStop();
    }

    @Override
    public void finish() {
        LogUtil.d("finish() --------------> ");
        super.finish();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d("onDestroy() --------------> ");
        mCheckedMap.clear();
        mCache.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(Menu.NONE, 0, 0, getResources().getString(R.string.apply)).setIcon(R.drawable.ic_menu_done).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        if (item.getItemId() == 0) {
            List<LocalAppInfo> list = mAppAdapter.getData();

            Collections.sort(list, new Comparator<LocalAppInfo>() {

                @Override
                public int compare(LocalAppInfo lhs, LocalAppInfo rhs) {
                    if (lhs.checkIndex != -1) {
                        return lhs.checkIndex - rhs.checkIndex;
                    }
                    return -1;
                }
            });

            StringBuilder checkedMapStr = new StringBuilder();
            for (int i = 0, N = list.size(); i < N; i++) {
                LocalAppInfo info = list.get(i);
                if (info.checkIndex >= 0) {
                    LogUtil.d("onClick ------------> " + info.checkIndex);
                    checkedMapStr = checkedMapStr.append(info.resolveInfo.activityInfo.name)
                            .append("#")
                            .append(info.resolveInfo.activityInfo.packageName)
                            .append("#")
                            .append(info.checkIndex)
                            .append("&&");
                }
            }

            int length = checkedMapStr.length();
            if (length > 2) {
                CharSequence charSequence = checkedMapStr.subSequence(0, length - 2);
                Settings.System.putString(getContentResolver(),  Constant.CHECKED_APP_MAP, charSequence.toString());
                Intent intent = new Intent(Constant.APP_MANAGER_ACTION);
                intent.putExtra("app_manager_value", charSequence);
                LockscreenAppManager.this.sendBroadcast(intent);
                LogUtil.d("onClick ---------> " + charSequence);
            } else {
                Settings.System.putString(getContentResolver(),  Constant.CHECKED_APP_MAP, "noapp");
                Intent intent = new Intent(Constant.APP_MANAGER_ACTION);
                intent.putExtra("app_manager_value", "noapp");
                LockscreenAppManager.this.sendBroadcast(intent);
                LogUtil.d("onClick --------->  null");
            }
            finish();
        }
        return true;
    }

    private void setAnimatorScale(View view, boolean isCheck) {
        if (isCheck) {
            mPvs = new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(View.SCALE_X, SCALE, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, SCALE, 1.0f)};
        } else {
            mPvs = new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, SCALE),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, SCALE)};
        }
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, mPvs);
        animator.setInterpolator(mTimeInterpolator);
        animator.setDuration(400);
        animator.start();
    }

    private void setScale(View view, boolean isCheck) {
        if (isCheck) {
            view.setScaleX(1f);
            view.setScaleY(1f);
        } else {
            view.setScaleX(SCALE);
            view.setScaleY(SCALE);
        }
    }

    private class AppAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<LocalAppInfo> mData = new ArrayList<LocalAppInfo>();
        private int mCurClickPosition = -1;

        private int[] mCheckImageIndex = {
                R.drawable.check_index1, R.drawable.check_index2,
                R.drawable.check_index3, R.drawable.check_index4,
                R.drawable.check_index5, R.drawable.check_index6
        };

        public AppAdapter(List<ResolveInfo> data) {
            mCheckedCount = 0;
            mInflater = LayoutInflater.from(LockscreenAppManager.this);
            for (ResolveInfo info : data) {
                if (sFilters.contains(info.activityInfo.packageName)) {
                    continue;
                }
                LocalAppInfo appInfo = new LocalAppInfo();
                appInfo.resolveInfo = info;
                appInfo.checkIndex = -1;
                mData.add(appInfo);
            }

            LogUtil.d("AppAdapter ------------> mCheckIndexMap = " + mCheckIndexMap);
            for (int i = 0, L = mData.size(); i < L; i++) {

                if (mCheckIndexMap.containsKey(mData.get(i).resolveInfo.activityInfo.name)) {
                    mData.get(i).checkIndex = mCheckIndexMap.get(mData.get(i).resolveInfo.activityInfo.name);
                    mCheckedCount++;
                } else {
                    mData.get(i).checkIndex = -1;
                }
            }

            Collections.sort(mData, new Comparator<LocalAppInfo>() {

                @Override
                public int compare(LocalAppInfo lhs, LocalAppInfo rhs) {
                    if (lhs.checkIndex != -1 && rhs.checkIndex == -1) {
                        return -1;
                    } else if (lhs.checkIndex == -1 && rhs.checkIndex != -1) {
                        return 1;
                    } else if (lhs.checkIndex != -1 && rhs.checkIndex != -1) {
                        return lhs.checkIndex - rhs.checkIndex;
                    } else {
                        return 0;
                    }
                }
            });

            mActionBar.setTitle(String.format(getResources().getString(R.string.checked_prompt), MAX_CHECKED - mCheckedCount));
            LogUtil.d("AppAdapter ------------> mCheckedCount = " + mCheckedCount);
        }

        public List<LocalAppInfo> getData() {
            return this.mData;
        }

        public void setData(List<LocalAppInfo> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.app_item,
                        null);
                holder.appIcon = (ImageView) convertView
                        .findViewById(R.id.img_icon);
                holder.name = (TextView) convertView
                        .findViewById(R.id.txt_name);
                holder.checkIndex = (ImageView) convertView.findViewById(R.id.img_index);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ResolveInfo info = mData.get(position).resolveInfo;
            holder.name.setText(info.loadLabel(mPM));

//            holder.appIcon.setImageDrawable(ThemeManager.getCustomizedIcon(info.loadIcon(mPM)));
//            new LoadIconTask(holder, info).execute();

            mExecutorService.execute(new LoadIconThread(holder, info));

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    LogUtil.d("onClick ------------> " + position);
                    if (mData.get(position).checkIndex == -1 && mCheckedCount >= MAX_CHECKED) {
                        Toast.makeText(LockscreenAppManager.this, LockscreenAppManager.this.getResources().getString(R.string.check_app_num_toast), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mCurClickPosition = position;
                    if (mData.get(position).checkIndex == -1) {
                        holder.checkIndex.setVisibility(View.VISIBLE);
                        mData.get(position).checkIndex = mCheckedCount;
                        mCheckedCount++;
                        setAnimatorScale(view, true);
                        LogUtil.d("onClick --------------> " + mData.get(position).checkIndex);
                    } else {
                        mCheckedCount--;
                        holder.checkIndex.setVisibility(View.GONE);
                        int tempIndex = mData.get(position).checkIndex;
                        mData.get(position).checkIndex = -1;
                        setAnimatorScale(view, false);
                        for (LocalAppInfo info : mData) {
                            if (info.checkIndex > tempIndex) {
                                LogUtil.d("onClick --------------> " + info.checkIndex);
                                info.checkIndex = info.checkIndex - 1;
                            }
                        }
                    }

                    notifyDataSetChanged();

                    mActionBar.setTitle(String.format(getResources().getString(R.string.checked_prompt), MAX_CHECKED - mCheckedCount));
                }
            });

            if (mData.get(position).checkIndex >= 0) {
                holder.checkIndex.setImageDrawable(getResources().getDrawable(mCheckImageIndex[mData.get(position).checkIndex]));
                holder.checkIndex.setVisibility(View.VISIBLE);
            } else {
                holder.checkIndex.setVisibility(View.GONE);
            }

            if (position != mCurClickPosition) {
                setScale(convertView, mData.get(position).checkIndex >= 0);
            }

            return convertView;
        }
    }

    class ViewHolder {
        public ImageView appIcon;
        public TextView name;
        public ImageView checkIndex;
    }

    public class LocalAppInfo {
        public int checkIndex = -1;
        public ResolveInfo resolveInfo;
    }


    private ExecutorService mExecutorService = Executors.newFixedThreadPool(5);//newCachedThreadPool();

    private class LoadIconThread implements Runnable{
        private ViewHolder mHolder;
        private ResolveInfo mResolveInfo;
        private Drawable mDrawable;

        public LoadIconThread(ViewHolder holder, ResolveInfo info){
            this.mHolder = holder;
            this.mResolveInfo = info;
        }

        @Override
        public void run() {
            if (mDrawable == null) {
//                mDrawable = BitmapCache.getInstance(LockscreenAppManager.this).loadIcon(mResolveInfo);
                mDrawable = ThemeManager.getInstance(LockscreenAppManager.this).loadIcon(mResolveInfo);;
                mHandler.sendEmptyMessage(1);
            }
        }

        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1 && mDrawable != null){
                    mHolder.appIcon.setImageDrawable(mDrawable);
                }
            }
        };
    }
//
//
//
//
//    private class LoadIconTask extends AsyncTask<String, Integer, String> {
//
//        private ViewHolder mHolder;
//        private ResolveInfo mResolveInfo;
//        private Drawable mDrawable;
//
//        public LoadIconTask(ViewHolder holder, ResolveInfo info) {
//            this.mHolder = holder;
//            this.mResolveInfo = info;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
////            try {
////                if (mCache.containsKey(mResolveInfo.activityInfo.name)){
////                    mDrawable =  mCache.get(mResolveInfo.activityInfo.name);
////                    if (mDrawable != null){
////                        return null;
////                    }
////                }
////                mDrawable = ThemeManager.getCustomizedIcon(mResolveInfo.loadIcon(mPM));
////                if (mDrawable != null){
////                    mCache.put(mResolveInfo.activityInfo.name, mDrawable);
////                }
////
////            } catch (Exception e) {
////                mDrawable = ThemeManager.getCustomizedIcon(getResources().getDrawable(R.drawable.recommendappicon));
////                e.printStackTrace();
////            }
//
//            if (mCache.containsKey(mResolveInfo.activityInfo.name)) {
//                mDrawable = mCache.get(mResolveInfo.activityInfo.name);
//                if (mDrawable != null) {
//                    return null;
//                }
//            }
//
////            String path = IconCustomizer.getFancyIconRelativePath(mResolveInfo.activityInfo.packageName, mResolveInfo.activityInfo.name);
////            if (path != null) {
////                mDrawable = lewa.laml.util.AppIconsHelper.getIconDrawable(LockscreenAppManager.this, path);
////            }
//
//            if (mDrawable == null) {
//                mDrawable = new FastBitmapDrawable(BitmapCache.getInstance(LockscreenAppManager.this).getIcon(mResolveInfo));
//            }
//
//            if (mDrawable != null) {
//                mCache.put(mResolveInfo.activityInfo.name, mDrawable);
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if (mDrawable != null) {
//                mHolder.appIcon.setImageDrawable(mDrawable);
//            }
//        }
//    }
}
