
package com.lewa.lockscreen2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


//import android.app.DownloadManager;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.LewaDownloadManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.Region;
import android.graphics.Rect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View.OnTouchListener;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.text.TextUtils;

import com.lewa.keyguard.newarch.KeyguardManager;
import com.lewa.lockscreen2.net.HttpRequest;
import com.lewa.lockscreen2.net.RecommendApp;
import com.lewa.lockscreen2.util.*;
import com.lewa.themes.ThemeManager;
import lewa.provider.ExtraSettings;
import lewa.content.ExtraIntent;
import lewa.util.ImageUtils;
import android.provider.Settings;
import com.android.internal.widget.LockPatternUtils;

public class LockscreenLayout extends RelativeLayout implements
        HandleViewGroup.OnDrawerOpenListener,
        HandleViewGroup.OnDrawerCloseListener,
        HandleViewGroup.OnDrawerScrollListener,
        OnItemClickListener, View.OnClickListener,
        Runnable {
    private static final boolean DEBUG_UNLOCK_ANIME = true;
    private static final int RECOMMEND_APP_COLUMN = 4;
    private static final String TIME_FORMAT_12 = "hh:mm";
    private static final String TIME_FORMAT_24 = "HH:mm";
    private static final String DATE_FORMAT = "MM.dd";//"MM月dd日";// "yyyy-MM-dd hh:mm:ss.SSS"
    private static boolean bDrawerFlag = false;
    private static boolean bScrollFlag = false;
    private static boolean isFirstStart = true;
    private static long sBBSDownloadId = -1;
    private static int wallpaperRotation;
    private static int mDownloadType;
    private static boolean isOpen;
    private long lastUpdateDownloadTime;
    private long wallpaperRotationTime;
    private long mWallpaperDownloadExpires;
    private LockPatternUtils mLockPatternUtils;
    private KeyguardManager mKeyguardManager;
    private LewaDownloadManager mLewaDownloadManager;
    private UpdateReceiver mUpdateReceiver;
    DownloadsChangeObserver mDownloadsObserver;
    TorchChangeObserver mTorchChangeObserver;
    private DbUtil mDbUtil;
    private BitmapCache mBitmapCache;
    private Lunar mLunar;
    private float mLastMotionX;
    private float mLastMotionY;

    private static String[] mWallpaperPath;
    private static String mLocalAppInfo;
    private static int mLocalAppCount;
    private int mRecommendAppSize;

    private Context mLockScreenContext;
    private Context mContext;
    private FixTextView mFixTime;//fix relayout
    private FixTextView mFixData;//fix relayout
    private ImageView mIBCamera;
    private TextView mTxtBBS;
    private TextView mTxtSettings;
    private ImageView mImgFlashlight;
    private ImageView mImgCalculator;
    private ImageView mImgDeskclock;
    private ImageView mImgFMRadio;
    //our drawer
    private HandleViewGroup mHandleView;//drawer handle
    private View mFollowerView;//hold the mBlurView
    private View mHandleContentGroups;//hold our content views
    private ImageView mBlurView;//blur background of drawer
    private Drawable mPaperDrawable;//default background of this
    //end drawer
    //our lock anime
    private Shader mShader;//lock screen line gradient shader
    private Paint mPaint;
    private GradientDrawable mMaskDrawable;//mask for background to against can't see time
    private RelativeLayout mMoveDownGroups;//move down anime view groups
    private int mMoveDownGroupsHeight = 0;//heigh of mMoveDownGroups
    //end anime
    private GridView mGridView;
    private AppAdapter mAppAdapter;
    private List<RecommendAppInfo> mRecommendsList;
    private Map<Long, String> mDownloadFirstMap;
    private Map<Long, RecommendAppInfo> mDownloadMap;
    private Map<Long, View> mDownloadViewMap;
    private long mLastUpdateDownloadTime;

    private HttpRequest mHttpRequest;
    private int mWidth;
    private int mHeight;
    private String[] week = null;
    private String[] amAndPm = null;

    private String[] mLocatWallpaper = {
            "bg/bg1.jpg", "bg/bg2.jpg",
            "bg/bg3.jpg", "bg/bg4.jpg",
            "bg/bg5.jpg", "bg/bg6.jpg",
            "bg/bg7.jpg", "bg/bg8.jpg"
    };

    public LockscreenLayout(Context context) {
        super(context);
        LogUtil.d("-------------> context:" + context);
    }

    public LockscreenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLockScreenContext = context;
        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mMaskDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x99000000, 0x00000000});
        mMaskDrawable.setShape(GradientDrawable.RECTANGLE);
        LogUtil.d("-------------> context:" + context);

        final ViewConfiguration vc = ViewConfiguration.get(context);
        mMaxVelocity = vc.getScaledMaximumFlingVelocity() / 1000.0f;
        mMinVelocity = vc.getScaledMinimumFlingVelocity() / 1000.0f;
        mTouchSlop = vc.getScaledTouchSlop();
        LogUtil.d("-------------> getApplicationContext:"
                + context.getApplicationContext());
        mContext = getContext();
        mUpdateReceiver = new UpdateReceiver();
        mTorchChangeObserver = new TorchChangeObserver(null);
        mDownloadsObserver = new DownloadsChangeObserver(null, mContext);
        register();
    }

    public LockscreenLayout(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtil.d("-------------> context:" + context);
    }

    private void setupView() {
        mMoveDownGroups = (RelativeLayout) findViewById(R.id.elements);
        mIBCamera = (ImageView) findViewById(R.id.img_lockscreen_camera);

        mGridView = (GridView) findViewById(R.id.gv_icon);
        mImgFlashlight = (ImageView) findViewById(R.id.img_flashlight);
        mImgCalculator = (ImageView) findViewById(R.id.img_calculator);
        mImgDeskclock = (ImageView) findViewById(R.id.img_deskclock);
        mImgFMRadio = (ImageView) findViewById(R.id.img_FMRadio);
        mTxtBBS = (TextView) findViewById(R.id.txt_bbs);
        mTxtSettings = (TextView) findViewById(R.id.txt_settings);

        mImgFlashlight.setOnClickListener(this);
        mImgCalculator.setOnClickListener(this);
        mImgDeskclock.setOnClickListener(this);
        mImgFMRadio.setOnClickListener(this);
        mTxtBBS.setOnClickListener(this);
        mTxtSettings.setOnClickListener(this);

        mGridView.setOnItemClickListener(this);

        mAppAdapter = new AppAdapter(mLockScreenContext);
        mIBCamera.setOnClickListener(this);

        week = mLockScreenContext.getResources().getStringArray(R.array.week);
        amAndPm = mLockScreenContext.getResources().getStringArray(R.array.amAndPm);

    }

    private void setLocalApp(List<RecommendAppInfo> list) {
        mLocalAppCount = 0;
        if ("noapp".equals(mLocalAppInfo)) {
            return;
        }
        String[] appInfo = mLocalAppInfo.toString().split("&&");
        LogUtil.d("setLocalApp() --------------> mLocalAppInfo:" + mLocalAppInfo);
        int length = appInfo.length;
        for (int i = 0; i < length; i++) {
            String[] str = appInfo[i].split("#");
            if (str[0].length() > 0 && str.length > 1) {
                if (!FileUtils.isExistsApk(mContext, str[1], str[0])) {
                    continue;
                }
                RecommendAppInfo item = new RecommendAppInfo();
                item.type = 1;
                item.packageName = str[1];
                item.className = str[0];
                mLocalAppCount++;
                list.add(item);
            }
        }
    }

    private void/*List<RecommendAppInfo>*/ refreshData() {
        if (mRecommendsList == null) {
            mRecommendsList = new ArrayList<RecommendAppInfo>();
        }
        mRecommendsList.clear();
        List<RecommendAppInfo> list = new ArrayList<RecommendAppInfo>();

        if (mLocalAppInfo != null && mLocalAppInfo.trim().length() > 0) {
            setLocalApp(list);
        } else {
            // load location appinfo
            mLocalAppCount = 0;
            String[] appInfo = Constant.DEFAULT_APPMANAGER.split("&&");
            int length = appInfo.length;
            for (int i = 0; i < length; i++) {
                String[] str = appInfo[i].split("#");
                if (str[0].length() > 0 && str.length > 1) {
                    RecommendAppInfo item = new RecommendAppInfo();
                    item.type = 1;
                    item.packageName = str[1];
                    item.className = str[0];
                    mLocalAppCount ++;
                    list.add(item);
                }
            }
        }

        // query db
        List<RecommendApp> recommendAppList = mDbUtil.queryAll();
        LogUtil.d("refreshData --------> dbList.size:" + recommendAppList.size());
        if (recommendAppList != null && recommendAppList.size() > 0) {
            HashMap<RecommendApp, Integer> tempMap = new HashMap<RecommendApp, Integer>();
            for (int i = 0, N = recommendAppList.size(); i < N; i++) {
                RecommendApp info = recommendAppList.get(i);
                if (FileUtils.isExistsApk(mContext, info.packageName)) {
                    tempMap.put(info, i);
                }
            }

            Set<Map.Entry<RecommendApp, Integer>> sets = tempMap.entrySet();
            for (Map.Entry<RecommendApp, Integer> entry : sets) {
                recommendAppList.remove(entry.getKey());
                LogUtil.d("refreshData --------> dbList.size:" + recommendAppList.size());
            }
            mRecommendAppSize = recommendAppList.size();

            int row = (1 + list.size()) / RECOMMEND_APP_COLUMN + 1;
            if (list.size() > 0 && recommendAppList.size() > 0) {
                for (int i = 0; i < list.size() && i < row && recommendAppList.size() > i; i++) {
                    RecommendApp recommendApp = recommendAppList.get(i);
                    RecommendAppInfo recommendAppInfo = new RecommendAppInfo();
                    recommendAppInfo.type = 0;
                    recommendAppInfo.name = recommendApp.name;
                    recommendAppInfo.iconName = recommendApp.icon_name;
                    recommendAppInfo.url = recommendApp.url;
                    recommendAppInfo.packageName = recommendApp.packageName;
                    list.add(i * RECOMMEND_APP_COLUMN, recommendAppInfo);
                }
            } else if (list.size() <= 0 && recommendAppList.size() > 0) {
                RecommendApp recommendApp = recommendAppList.get(0);
                RecommendAppInfo recommendAppInfo = new RecommendAppInfo();
                recommendAppInfo.type = 0;
                recommendAppInfo.name = recommendApp.name;
                recommendAppInfo.iconName = recommendApp.icon_name;
                recommendAppInfo.url = recommendApp.url;
                recommendAppInfo.packageName = recommendApp.packageName;
                list.add(0, recommendAppInfo);
            }
        }

        if (/*list.size() < RECOMMEND_APP_COLUMN * 2 && */mLocalAppCount < 6) {
            RecommendAppInfo recommendAppInfo = new RecommendAppInfo();
            recommendAppInfo.type = 2;
            recommendAppInfo.name = getResources().getString(R.string.add);
            list.add(recommendAppInfo);
        }

        mRecommendsList.addAll(list);
        LogUtil.d("refreshData() -----------> size:" + list.size());
    }

    private void getLocalApp(String packageName, String className) {
        RecommendAppInfo item = new RecommendAppInfo();
        try {
            ApplicationInfo info = mLockScreenContext.getPackageManager().getApplicationInfo(
                    packageName, 0);
            item.type = 1;
            item.name = info.name;
            item.packageName = packageName;
            item.className = className;
        } catch (NameNotFoundException e) {
            LogUtil.d("getLocalApp-----------> packageName:" + packageName + ", e:"
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    private void init() {
        mBitmapCache = BitmapCache.getInstance(mContext);
        mBitmapCache.clear();

        mLunar = new Lunar(mLockScreenContext);

        if (mLockPatternUtils == null) {
            mLockPatternUtils = new LockPatternUtils(mContext);
        }

        if (mDownloadFirstMap == null) {
            mDownloadFirstMap = new HashMap<Long, String>();
        }

        if (mDownloadMap == null) {
            mDownloadMap = new HashMap<Long, RecommendAppInfo>();
        }

        if (mDownloadViewMap == null) {
            mDownloadViewMap = new HashMap<Long, View>();
        }

        if (mDbUtil == null) {
            mDbUtil = new DbUtil(mContext);
        }

        if (mHttpRequest == null) {
            mHttpRequest = new HttpRequest(mContext, mLockScreenContext);
        }

        if (mWallpaperPath == null || mWallpaperPath.length == 0) {
            mWallpaperPath = FileUtils.getWallpaperPath();
        }

        LogUtil.d("init() -----------> isFirstStart:" + isFirstStart);
        if (isFirstStart) {
            initNotiExtras();
            wallpaperRotation = SharedPreferencesUtil.getSettingInt(mLockScreenContext,
                    Constant.WALLPAPER_ROTATION_TYPE,
                    Constant.WallpaperRotation.SCREEN_ON.ordinal());
            wallpaperRotationTime = Settings.System.getLong(mContext.getContentResolver(), Constant.WALLPAPER_ROTATION_TIME, 0);
            mDownloadType = SharedPreferencesUtil.getSettingInt(mLockScreenContext, Constant.NETWORK_DOWNLOAD_TYPE,
                    Constant.DownloadType.WIFI_UPDATE.ordinal());
            mLocalAppInfo = Settings.System.getString(mContext.getContentResolver(), Constant.CHECKED_APP_MAP);
            mWallpaperDownloadExpires = Settings.System.getLong(mContext.getContentResolver(), Constant.WALLPAPER_DOWNLOAD_EXPIRES, 0);

            LogUtil.d("init() -----------> wallpaperRotation:" + wallpaperRotation + ", wallpaperRotationTime:" + wallpaperRotationTime + ", mDownloadType:" + mDownloadType);
            LogUtil.d("init() -----------> mLocalAppInfo:" + mLocalAppInfo);
            //updateWallpaper();
            String curPath = Settings.System.getString(mContext.getContentResolver(), Constant.WALLPAPER_CURRENT_PATH);
            LogUtil.d("init() -----------> curPath:" + curPath);
            if (TextUtils.isEmpty(curPath) || !new File(curPath).exists()) {
                int index = (int) (Math.random() * mLocatWallpaper.length);
                refreshPaperResource(mLocatWallpaper[index]);
            }
            setWallpaper(curPath);
            isFirstStart = false;
        }
        notifyDataSetChanged();

    }

    private void appCommendChanged() {
        refreshData();
        mAppAdapter.setData(mRecommendsList);
        mAppAdapter.notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        refreshData();
        mAppAdapter.setData(mRecommendsList);
        LogUtil.d("notifyDataSetChanged() ---------------> ");
        mGridView.setAdapter(mAppAdapter);
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Constant.WALLPAPER_ROTATION_TYPE_ACTION);
        filter.addAction(Constant.NETWORK_DOWNLOAD_TYPE_ACTION);
        filter.addAction(Constant.APP_MANAGER_ACTION);
        filter.addAction(Constant.REFRESH_DATA_ACTION);
        filter.addAction(Constant.REFRESH_DATA_COMPLETE_ACTION);
//        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        mContext.registerReceiver(mUpdateReceiver, filter);
        mContext.getContentResolver().registerContentObserver(LewaDownloadManager.CONTENT_URI, true, mDownloadsObserver);
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(ExtraSettings.System.TORCH_STATE), true, mTorchChangeObserver);
    }
    private void unRegister() {
        mContext.unregisterReceiver(mUpdateReceiver);
        mContext.getContentResolver().unregisterContentObserver(mDownloadsObserver);
        mContext.getContentResolver().unregisterContentObserver(mTorchChangeObserver);

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        LogUtil.d("onConfigurationChanged -------------------->");
        if (mTxtBBS != null && mTxtSettings != null){
            mTxtBBS.setText(getResources().getString(R.string.lockscreen_bbs));
            mTxtSettings.setText(getResources().getString(R.string.lockscreen_settings));
        }
        super.onConfigurationChanged(newConfig);
    }

    public void onAttachToKeyguard(KeyguardManager keyguardManager) {
        long startTime = System.currentTimeMillis();
        LogUtil.d("onAttachToKeyguard ----------> ");
        mContext = getContext();
        mKeyguardManager = keyguardManager;
        mLewaDownloadManager = LewaDownloadManager.getInstance(mContext.getContentResolver(), mContext.getPackageName());
        setupView();
        FileUtils.initWallpaperFile();
        init();

        updateTimeAndData();

        LogUtil.d("onAttachToKeyguard ----------> end:" + (System.currentTimeMillis() - startTime));
    }

    public void onDetachFromKeyguard() {
        LogUtil.d("onDetachFromKeyguard()--------> " + System.currentTimeMillis());
        // unRegister();
        mBitmapCache.clear();
    }

    public void showKeyguard() {
        LogUtil.d("showKeyguard()--------> ");
//        this.setAlpha(1.0f);
        mMoveDownGroups.setAlpha(1.0f);
        reset();
        updateTimeAndData();
        onBouncerHide();
    }

    //    long curTime = 0;
    public void hideKeyguard() {
//        curTime = System.currentTimeMillis();
//        LogUtil.d("hideKeyguard()--------> " + System.currentTimeMillis());
        //mSlidingDrawer.close();
        mHandleView.closeNoAnime();
        bDrawerFlag = false;
        bScrollFlag = false;
        updateWallpaper();

        LogUtil.d("hideKeyguard()--------> wallpaperDownloadExpires:"
                + mWallpaperDownloadExpires);

        Long serverTimeDiff = android.provider.Settings.System.getLong(mContext.getContentResolver(), Constant.SERVER_TIME_DIFF, 0l);
        if ((System.currentTimeMillis() + serverTimeDiff) > mWallpaperDownloadExpires) {
            LogUtil.d("hideKeyguard() updateImage --------> currentTime > wallpaperDownloadExpires, updateImage");
            mHttpRequest.updateImage(mDownloadType, mWidth, mHeight, null);
        } else {
            LogUtil.d("hideKeyguard() updateImage --------> currentTime < wallpaperDownloadExpires, not updateImage");
        }
    }

    boolean bShowBouncer = false;

    public void onBouncerShow() {
        LogUtil.d("onBouncerShow()--------> ");
        //this.setVisibility(View.GONE);
        //mHandleView.closeNoAnime();
        mMoveDownGroups.setAlpha(0.0f);
        mState = STATE_OPEN;
        mHandleView.closeNoAnime();
        setTranslatePercent(100);
        invalidate();
        bShowBouncer = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (bShowBouncer)
            return true;
        else
            return super.dispatchTouchEvent(ev);
    }

    public void onBouncerHide() {
        LogUtil.d("onBouncerHide()--------> ");
        bShowBouncer = false;
        this.setAlpha(1.0f);
        this.setVisibility(View.VISIBLE);
        showBackgroud();
        mMoveDownGroups.setAlpha(1.0f);
    }

    public void onLockOpened() {
        LogUtil.d("onLockOpened()--------> " + System.currentTimeMillis());
        mMoveDownGroups.setAlpha(0.0f);
        mKeyguardManager.dismiss();
    }

    public void onLockClosed() {
        LogUtil.d("onLockClosed()--------> " + System.currentTimeMillis());
    }

    public void cleanUp() {
        LogUtil.d("cleanUp()--------> ");
        isFirstStart = true;
        sBBSDownloadId = -1;
        mBitmapCache.clear();
        mRecommendsList.clear();
        mDownloadMap.clear();
        mDownloadViewMap.clear();
        mKeyguardManager = null;
        mLewaDownloadManager = null;
        mDbUtil = null;
        mHttpRequest = null;
        mAppAdapter = null;
        recycleBackground();
        unRegister();
        this.removeAllViewsInLayout();
        mLockScreenContext = null;
        mContext = null;
        System.gc();
    }

    private void updateTimeAndData() {

        LogUtil.d("updateTimeAndData()--------> ");
        String timeStandard = android.provider.Settings.System.getString(mContext.getContentResolver(), Settings.System.TIME_12_24);
        String timeFormat;
        if ("24".equals(timeStandard)) {
            timeFormat = TIME_FORMAT_24;
        } else {
            timeFormat = TIME_FORMAT_12;
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
        mFixTime.setTimeString(format.format(date));
        mFixData.setTimeString(getDate(date));
        // mWebView.loadUrl("javascript:updateTime('" + format.format(date) +
        // "', '" + getDate(date) + "')");
        // // String layout = "#timeControls{font-size:6.5rem;}";
        // // String layout =
        // "#timeControls{\\r\\n  font-size:36px;\\r\\n  position:absolute;\\r\\n  left:30px;\\r\\n  top:50px;\\r\\n}\\r\\n#dateControls{\\r\\n  font-size:14px;\\r\\n  position:absolute;\\r\\n  left:30px;\\r\\n  top:90px;\\r\\n}";
        // String layout =
        // "#timeControls{\\r\\n  font-size:36px;\\r\\n  position:absolute;\\r\\n  left:30px;\\r\\n  top:50px;\\r\\n}";
        // mWebView.loadUrl("javascript:insertCss('" + layout + "')");
    }

    private String getDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dateOfWeek = c.get(Calendar.DAY_OF_WEEK);

        String language = mContext.getResources().getConfiguration().locale.getLanguage();
        if (language.endsWith("zh")) {
            mLunar.setDate(date, mLockScreenContext);
            return new SimpleDateFormat(DATE_FORMAT).format(date) + " "
                    + week[dateOfWeek - 1] + "  " + mLunar.getLunar();
        } else {
            String amPm = amAndPm[0];
            if (c.get(Calendar.AM_PM) == 0) {
                amPm = amAndPm[0];
            } else {
                amPm = amAndPm[1];
            }
            return new SimpleDateFormat(DATE_FORMAT).format(date) + " "
                    + week[dateOfWeek - 1] + amPm;
        }
    }

    private long lastUpdateWallpaperTime;

    private void updateWallpaper() {
        if (System.currentTimeMillis() - lastUpdateDownloadTime < 5000) {
            LogUtil.d("updateWallpaper() --------> currentTime - lastUpdateDownloadTime < 5000, not updatewallpaper");
            return;
        }
        lastUpdateDownloadTime = System.currentTimeMillis();

        LogUtil.d("updateWallpaper()--------> diff:"
                + (System.currentTimeMillis() - wallpaperRotationTime) + ", "
                + wallpaperRotation);

        if (wallpaperRotation == Constant.WallpaperRotation.ONE_DAY.ordinal()) {
            if (System.currentTimeMillis() - wallpaperRotationTime < Constant.ONE_DAY_MS) {
                LogUtil.d("updateWallpaper() --------> diff < one day, not updatewallpaper");
                return;
            }
        } else if (wallpaperRotation == Constant.WallpaperRotation.ONE_HOUR.ordinal()) {
            if (System.currentTimeMillis() - wallpaperRotationTime < Constant.ONE_HOUR_MS) {
                LogUtil.d("updateWallpaper() --------> diff < one hour, not updatewallpaper");
                return;
            }
        } else if (wallpaperRotation == Constant.WallpaperRotation.SCREEN_ON
                .ordinal()) {
            LogUtil.d("updateWallpaper()--------> SCREEN_ON");
        } else {
            String curPath = Settings.System.getString(mContext.getContentResolver(), Constant.WALLPAPER_CURRENT_PATH);
            setWallpaper(curPath);
            return;
        }

        wallpaperRotationTime = System.currentTimeMillis();
        Settings.System.putLong(mContext.getContentResolver(), Constant.WALLPAPER_ROTATION_TIME, System.currentTimeMillis());

        if (mWallpaperPath == null || mWallpaperPath.length == 0) {
            mWallpaperPath = FileUtils.getWallpaperPath();
        }

        if (mWallpaperPath == null || mWallpaperPath.length == 0) {
            LogUtil.d("updateWallpaper() --------> mWallpaperPath is null or 0 , set local wallpaper");
            int index = (int) (Math.random() * mLocatWallpaper.length);
            refreshPaperResource(mLocatWallpaper[index]);
            return;
        } else if (mWallpaperPath.length <= 2) {
            LogUtil.d("updateWallpaper() --------> mWallpaperPath.length:" + mWallpaperPath.length);
            int index = (int) (Math.random() * (mLocatWallpaper.length + mWallpaperPath.length));
            if (index < mLocatWallpaper.length) {
                refreshPaperResource(mLocatWallpaper[index]);
            } else {
                Settings.System.putString(mContext.getContentResolver(), Constant.WALLPAPER_CURRENT_PATH, mWallpaperPath[index - mLocatWallpaper.length]);
                setWallpaper(mWallpaperPath[index - mLocatWallpaper.length]);
            }
            return;
        }

        LogUtil.d("updateWallpaper()--------> mWallpaperPath.length:"
                + mWallpaperPath.length);
        int index = (int) (Math.random() * mWallpaperPath.length);
        Settings.System.putString(mContext.getContentResolver(), Constant.WALLPAPER_CURRENT_PATH, mWallpaperPath[index]);
        setWallpaper(mWallpaperPath[index]);
    }

    private void setWallpaper(String path) {
        if (path == null) {
            path = "";
        }

        LogUtil.d("setWallpaper()--------> path:" + path);
        File file = new File(path);
        if (file.exists()) {
            new BackgroundTask(file.getAbsolutePath(), mContext).execute();
        } else {
            // update mWallpaperPath
            mWallpaperPath = FileUtils.getWallpaperPath();
        }
    }

    private void recycleBackground() {
        LogUtil.d("recycleBackground()--------> drawable:");
        if (mPaperDrawable != null) {
            mPaperDrawable.setCallback(null);
            ((BitmapDrawable) mPaperDrawable).getBitmap().recycle();
            mPaperDrawable = null;
        }
        Drawable drawable = mBlurView.getDrawable();
        mBlurView.setImageResource(0);
        if (drawable != null) {
            drawable.setCallback(null);
            ((BitmapDrawable) drawable).getBitmap().recycle();
            drawable = null;
        }
        if (mBlurDrawable != null) {
            mBlurDrawable.setCallback(null);
            ((BitmapDrawable) mBlurDrawable).getBitmap().recycle();
            mBlurDrawable = null;
        }
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mFixTime = (FixTextView) findViewById(R.id.ftxt_time);
        mFixData = (FixTextView) findViewById(R.id.ftxt_data);
        mHandleView = (HandleViewGroup) findViewById(R.id.my_handle);
        mBlurView = (ImageView) findViewById(R.id.img_blur);
        mHandleContentGroups = findViewById(R.id.content_group);
        mFollowerView = findViewById(R.id.follower_group);
        mBlurView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                return true;
            }
        });
        mHandleView.setContentView(mHandleContentGroups);
        mHandleView.setFollowerView(mFollowerView);
        mHandleView.setBlurView(mBlurView);
        mHandleView.setOnDrawerCloseListener(this);
        mHandleView.setOnDrawerOpenListener(this);
        mHandleView.setOnDrawerScrollListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("onMeasure()--------> ");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = measureWidth(widthMeasureSpec);
        int measureHeight = measureHeight(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("onLayout()--------> ");
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mMoveDownGroupsHeight = mFixData.getHeight() + mFixTime.getHeight() + DensityUtil.dip2px(getContext(), 47);
        mGrandientWidth = (int) (getWidth() * GRADIENT_PERCENT);
        if (mPaperDrawable != null)
            mPaperDrawable.setBounds(0, 0, getWidth(), getHeight());
        if (mBlurDrawable != null)
            mBlurDrawable.setBounds(0, 0, getWidth(), getHeight());
        if (mMaskDrawable != null)
            mMaskDrawable.setBounds(0, getHeight() - DensityUtil.dip2px(getContext(), 154), getWidth(), getHeight());
    }

    private int measureWidth(int pWidthMeasureSpec) {
        int result = 0;
        int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);
        int widthSize = MeasureSpec.getSize(pWidthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = widthSize;
                break;
        }
        return result;
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;

        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }

    private String mLastPaperPath;

    public void refreshPaperResource(String path) {
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("refreshPaperResource " + path);
        if (path != null && !path.equals(mLastPaperPath)) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeStream(mContext.getAssets().open(path), null, options);//decodeResource(getResources(), res);
                if(options.outWidth > Constant.MAX_PIC_WIDTH) {
                    options.outHeight = options.outHeight * Constant.MAX_PIC_WIDTH / options.outWidth;
                    options.inSampleSize = options.outWidth / Constant.MAX_PIC_WIDTH;
                    options.outWidth = Constant.MAX_PIC_WIDTH;
                    options.inJustDecodeBounds = false;
                    bmp = BitmapFactory.decodeStream(mContext.getAssets().open(path), null, options);
                } else {
                    bmp = BitmapFactory.decodeStream(mContext.getAssets().open(path));
                }
                BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
                refreshPaperDrawable(drawable);
                mLastPaperPath = path;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void refreshPaperDrawable(Drawable drawable) {
        if (mPaperDrawable != drawable) {
            recycleBackground();
            mPaperDrawable = drawable;
            mPaperDrawable.setBounds(0, 0, getWidth(), getHeight());
            Drawable draw = getBlurDrawable(drawable);
            draw.setBounds(0, 0, getWidth(), getHeight());
            mBlurDrawable = draw.getConstantState().newDrawable();
            mBlurDrawable.setBounds(0, 0, getWidth(), getHeight());
            mBlurView.setImageDrawable(draw);
            mBlurView.setColorFilter(new PorterDuffColorFilter(0x1A000000, Mode.SRC_ATOP));
            invalidate();
        }
    }

    public Drawable getBlurDrawable(Drawable bd) {
        Bitmap screenshot = ((BitmapDrawable) bd).getBitmap();
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("getBlurDrawable " + screenshot);
        Bitmap in = Bitmap.createScaledBitmap(screenshot, 64, 113, true);
        Bitmap out = Bitmap.createBitmap(64, 113, Bitmap.Config.ARGB_8888);
        ImageUtils.fastBlur(in, out, 18);
        in.recycle();

        BitmapDrawable drawable = new BitmapDrawable(getResources(), out);
        drawable.setFilterBitmap(true);
        return drawable;
    }

    private long mLastTick;
    private float mOrignTranslateWidth;
    private float mLastTranslateWidth;
    private boolean mForceStop = false;
    private static final int FINISH_TIME = 150;

    @Override
    public void run() {
        long tt = System.nanoTime();
        float dt = (tt - mLastTick) / 1000000.0f; /* millisecond */
        dt = dt > 30 ? 30 : dt;
        dt = dt < 10 ? 17 : dt;
        mLastTick = tt;
        if (mOrignTranslateWidth < 0) {
            mLastTranslateWidth += dt * (-1.1 + mOrignTranslateWidth) / FINISH_TIME;
        } else {
            mLastTranslateWidth += dt * (1.1 - mOrignTranslateWidth) / FINISH_TIME;
        }
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("run anime dt=" + dt + " ow=" + mOrignTranslateWidth + " cw=" + mLastTranslateWidth);
        if (Math.abs(mLastTranslateWidth) >= 1.1) {
            mState = STATE_OPEN;
            setTranslatePercent(100);
            invalidate();
            onLockOpened();
            return;
        }
        setTranslatePercent(mLastTranslateWidth);
        invalidate();
        postOnAnimation(this);
    }

    float mDownX, mDownY;//down event x and y cor
    float mGradientEndX;
    private VelocityTracker mVelocity;
    int mDirection = 0;//-1:left, 1:right
    private int mTouchSlop;   // Distance to travel before a drag may begin
    private float mMaxVelocity;
    private float mMinVelocity;
    public static final int STATE_IDLE = 0,
            STATE_DRAGING = 1,
            STATE_OPEN = 2;
    private int mState = STATE_IDLE;

    public boolean isStateIdle() {
        return mState == STATE_IDLE;
    }

    private void showBackgroud() {
        mState = STATE_IDLE;
        mGradientEndX = 0;
        setTranslatePercent(0);
        invalidate();
        onLockClosed();
    }

    private void reset() {
        mGradientEndX = 0;
        mHandleView.closeNoAnime();
        setTranslatePercent(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("ACTION_EVENT " + event.getAction() + "count " + event.getPointerCount() + "   cx:" + event.getX(0));
        if (mVelocity == null) {
            mVelocity = VelocityTracker.obtain();
        }
        mVelocity.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mKeyguardManager.userActivity();
                if (mHandleView.isOpened()) {
                    mHandleView.closeHandle();
                    return false;
                }
                mDirection = 0;
                mDownX = event.getX();
                mDownY = event.getY();
                mState = STATE_IDLE;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mDownX;
                float dy = event.getY() - mDownY;
                if (DEBUG_UNLOCK_ANIME)
                    LogUtil.d(mTouchSlop + " dx " + dx + " dy " + dy + " xie " + (dx / dy) + "  direction:" + mDirection);
                if (dx > mTouchSlop || (dx >= 0 && dx <= mTouchSlop && mDirection == 1)) {
                    if (mState != STATE_DRAGING && Math.abs(dy / dx) > 1.732) {
                        break;
                    }
                    if (DEBUG_UNLOCK_ANIME)
                        LogUtil.d("start move");
                    mState = STATE_DRAGING;
                    if (mDirection < 0) { // do not revert direction
                        mGradientEndX = 0;
                        setTranslatePercent(0);
                        invalidate();
                        break;
                    } else {
                        mGradientEndX = 2 * dx;
                        mDirection = 1;
                    }
                    setTranslatePercent(mGradientEndX / getWidth());
                    invalidate();
                } else if (dx < -mTouchSlop || (dx <= 0 && dx >= -mTouchSlop && mDirection == -1)) {
                    if (mState != STATE_DRAGING && Math.abs(dy / dx) > 1.732) {
                        break;
                    }
                    if (DEBUG_UNLOCK_ANIME)
                        LogUtil.d("start move");
                    mState = STATE_DRAGING;
                    if (mDirection > 0) {
                        mGradientEndX = 0;
                        setTranslatePercent(0);
                        invalidate();
                        break;
                    } else {
                        mGradientEndX = 2 * dx;
                        mDirection = -1;
                    }
                    setTranslatePercent(mGradientEndX / getWidth());
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                mVelocity.computeCurrentVelocity(1);
                float vx = mVelocity.getXVelocity();
                final int width = getWidth();
                if (DEBUG_UNLOCK_ANIME)
                    LogUtil.d(mMinVelocity + " velocity " + vx + " width:" + width);
                if (Math.abs(mGradientEndX) >= width) {
                    if (DEBUG_UNLOCK_ANIME)
                        LogUtil.d("lock open" + mGradientEndX + "---------" + width);
                    mState = STATE_OPEN;
                    setTranslatePercent(100);
                    onLockOpened();
                } else if (mState == STATE_DRAGING && ((vx > mMinVelocity && mDirection > 0) || (vx < -mMinVelocity && mDirection < 0))) {
                    if (DEBUG_UNLOCK_ANIME)
                        LogUtil.d("velocity open");
                    mState = STATE_DRAGING;
                    mLastTranslateWidth = mGradientEndX / width;
                    mOrignTranslateWidth = mLastTranslateWidth;
                    mLastTick = System.nanoTime();
                    postOnAnimation(this);
                } else {
                    if (DEBUG_UNLOCK_ANIME)
                        LogUtil.d("lock close" + mGradientEndX + "---------" + width);
                    mState = STATE_IDLE;
                    mGradientEndX = 0;
                    setTranslatePercent(0);
                    invalidate();
                    onLockClosed();
                }
                mVelocity.clear();
                break;
        }
        return true;
    }

    //time move down anime interpolation
    private float getInterpolation(float top) {
        float t = (float) (Math.pow(top, 1.5) * 2);
        t = t < 0 ? 0 : t;
        t = t > 1 ? 1 : t;
        return t;
    }

    private void setMoveDownGroupsTop(float top) {
        float t = getInterpolation(top);
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("setMoveDownGroupsTop:" + top + " rt:" + t + " height:" + mMoveDownGroupsHeight);
        mMoveDownGroups.offsetTopAndBottom((int) (t * mMoveDownGroupsHeight - mMoveDownGroups.getTop()));
    }

    private void setTranslateWidth(int width) {
        this.mTranslateWidth = width;
        if (mTranslateWidth > getWidth() + mGrandientWidth) {
            mTranslateWidth = getWidth() + mGrandientWidth;
        } else if (mTranslateWidth < -getWidth() - mGrandientWidth) {
            mTranslateWidth = -getWidth() - mGrandientWidth;
        }
    }

    private static int MAX_START_ALPHA_SECURE = 0;//255 * 0.8;
    private static int MAX_START_ALPHA = 220;//255 * 0.8;
    private static float GRADIENT_PERCENT = 0.3f;
    private int mGrandientWidth = 0; // GRADIENT_PERCENT * getWidth

    //translate percent, positive from left to right and negitive from right to left; 0 is static
    private void setTranslatePercent(float t) {
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("setTranslatePercent:" + t);
        setMoveDownGroupsTop(Math.abs(t));
        setTranslateWidth((int) (t * getWidth()));
        Shader shader = null;
        if (t == 0) {//close idle
            shader = null;
        } else if (t == 100) {//here is open status
            shader = new LinearGradient(0, 0, getWidth(), 0, 0x00FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
        } else if (t > 0) { //from left to right
            if (mKeyguardManager.isSecure()) {
                float start = t < GRADIENT_PERCENT ? 0 : (t - GRADIENT_PERCENT);
                shader = new LinearGradient((int) (start * getWidth()), 0, (int) (t * getWidth()), 0, 0x00FFFFFF, 0xFFFFFFFF, Shader.TileMode.CLAMP);
            } else {
                int alpha = (int) (MAX_START_ALPHA * (1 - t));
                alpha = alpha < 0 ? 0 : alpha;
                alpha = 0x00FFFFFF | (alpha << 24);
                shader = new LinearGradient(0, 0, (int) (t * getWidth()), 0, alpha, 0xFFFFFFFF, Shader.TileMode.CLAMP);
            }
        } else {//from right to left
            if (mKeyguardManager.isSecure()) {
                float start = t > -GRADIENT_PERCENT ? 0 : (t + GRADIENT_PERCENT);
                shader = new LinearGradient((int) ((1 + start) * getWidth()), 0, (int) ((1 + t) * getWidth()), 0, 0x00FFFFFF, 0xFFFFFFFF, Shader.TileMode.CLAMP);
            } else {
                int alpha = (int) (MAX_START_ALPHA * (1 + t));
                alpha = alpha < 0 ? 0 : alpha;
                alpha = 0x00FFFFFF | (alpha << 24);
                shader = new LinearGradient(getWidth(), 0, (int) ((1 + t) * getWidth()), 0, alpha, 0xFFFFFFFF, Shader.TileMode.CLAMP);
            }
        }
        mShader = shader;

    }

    int mTranslateWidth = 0;
    Drawable mBlurDrawable;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (DEBUG_UNLOCK_ANIME)
            LogUtil.d("dispatchDraw width:" + mTranslateWidth + " state=" + isStateIdle() + " mShader=" + (mShader != null));
        if (!isStateIdle() && mShader != null && mTranslateWidth != 0) {
            if (mKeyguardManager.isSecure()) {
                int start = 0;
                int end = 0;
                if (mTranslateWidth > 0) {//left to right
                    if (mTranslateWidth < getWidth()) {
                        canvas.save();
                        canvas.clipRect(0, 0, mTranslateWidth, getHeight());
                        if (mBlurDrawable != null)
                            mBlurDrawable.draw(canvas);
                        canvas.restore();
                        canvas.save();
                        canvas.clipRect(mTranslateWidth, 0, getWidth(), getHeight());
                        if (mPaperDrawable != null)
                            mPaperDrawable.draw(canvas);
                        canvas.restore();
                    } else {
                        if (mBlurDrawable != null)
                            mBlurDrawable.draw(canvas);
                    }
                    if (mTranslateWidth < mGrandientWidth) {
                        start = 0;
                        end = mTranslateWidth;
                    } else if (mTranslateWidth < getWidth() + mGrandientWidth) {
                        start = mTranslateWidth - mGrandientWidth;
                        end = start + mGrandientWidth;
                    } else {
                        start = getWidth() - 1;
                        end = getWidth();
                    }
                    canvas.saveLayer(start, 0, end, canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
                    if (mPaperDrawable != null)
                        mPaperDrawable.draw(canvas);
                    mPaint.setShader(mShader);
                    canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
                    canvas.restore();
                } else {//right to left
                    if (mTranslateWidth > -getWidth()) {
                        canvas.save();
                        canvas.clipRect(getWidth() + mTranslateWidth, 0, getWidth(), getHeight());
                        if (mBlurDrawable != null)
                            mBlurDrawable.draw(canvas);
                        canvas.restore();
                        canvas.save();
                        canvas.clipRect(0, 0, getWidth() + mTranslateWidth, getHeight());
                        if (mPaperDrawable != null)
                            mPaperDrawable.draw(canvas);
                        canvas.restore();
                    } else {
                        mBlurDrawable.draw(canvas);
                    }
                    if (mTranslateWidth > -mGrandientWidth) {
                        start = 0;
                        end = mTranslateWidth;
                    } else if (mTranslateWidth > -getWidth() - mGrandientWidth) {
                        start = mTranslateWidth + mGrandientWidth;
                        end = start - mGrandientWidth;
                    } else {
                        start = -getWidth() + 1;
                        end = -getWidth();
                    }
                    canvas.saveLayer(canvas.getWidth() + end, 0, canvas.getWidth() + start, canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
                    if (mPaperDrawable != null)
                        mPaperDrawable.draw(canvas);
                    mPaint.setShader(mShader);
                    canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
                    canvas.restore();
                }
                mMaskDrawable.draw(canvas);
                super.dispatchDraw(canvas);
            } else {
                if (mPaperDrawable != null)
                    mPaperDrawable.draw(canvas);
                mMaskDrawable.draw(canvas);
                super.dispatchDraw(canvas);
                mPaint.setShader(mShader);
                canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
            }
        } else {
            if (mPaperDrawable != null)
                mPaperDrawable.draw(canvas);
            mMaskDrawable.draw(canvas);
            super.dispatchDraw(canvas);
        }
    }

    @Override
    public void onDrawerOpened() {
        LogUtil.d("onDrawerOpened()--------> ");
        bDrawerFlag = true;
        this.requestFocus(); // add for problem that first time back and home key no use
        mIBCamera.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDrawerClosed() {
        LogUtil.d("onDrawerClosed()--------> ");
        bDrawerFlag = false;
        //mIBCamera.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScrollStarted() {
        LogUtil.d("onScrollStarted()--------> ");
        bScrollFlag = true;
        mIBCamera.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onScrollEnded() {
        LogUtil.d("onScrollEnded()--------> ");
        bScrollFlag = false;
//        mIBCamera.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        LogUtil.d("onClick()--------> ");
        switch (v.getId()) {
            case R.id.txt_settings:
                launchActivity("com.lewa.lockscreen2", "com.lewa.lockscreen2.LockscreenSettings", false);
                break;
            case R.id.txt_bbs:

                boolean isInstall = false;
                try {
                    mContext.getPackageManager().getApplicationInfo("com.lewa.userfeedback", PackageManager.GET_UNINSTALLED_PACKAGES);
                    isInstall = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    isInstall = false;
                }
                if (isInstall) {
                    launchActivity("com.lewa.userfeedback", "com.lewa.userfeedback.MainActivity", false);
                } else {
                    String name = getResources().getString(R.string.lockscreen_bbs);
                    if (mDownloadMap.containsKey(sBBSDownloadId)) {
                        LogUtil.d("txt_bbs ---------> bbs is downloading");
                        return;
                    }

                    LewaDownloadManager.Request request = new LewaDownloadManager.Request(Uri.parse(Constant.BBS_URL));
                    try {
                        request.setDestinationInExternalPublicDir(Constant.RECOMMENDAPP_APP, name + ".apk");
                    } catch (Exception e) {
                        LogUtil.e("setDestinationInExternalPublicDir ---------> error:" + e.getMessage());
                        e.printStackTrace();
                    }
                    request.setNotiExtras(name);
                    request.setMimeType("application/vnd.android.package-archive");
                    request.setShowRunningNotification(true);
                    request.setNotificationVisibility(LewaDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI);
                    long downloadId = mLewaDownloadManager.enqueue(request);
                    sBBSDownloadId = downloadId;
                    mDownloadMap.put(downloadId, null);
                    LogUtil.e("onClick ---------> txt_bbs  downloadId:" + downloadId);
                }

                break;
            case R.id.img_flashlight:
//                launchActivity("com.lewa.flashlight",
//                        "com.lewa.flashlight.FlashlightActivity", true);
                launchFlashLight();
                break;
            case R.id.img_calculator:
                launchActivity("com.android.calculator2", "com.android.calculator2.Calculator", true);
                break;
            case R.id.img_deskclock:
                launchActivity("com.android.deskclock",
                        "com.android.deskclock.DeskClockMainActivity", false);
                break;
            case R.id.img_FMRadio:
                launchActivity("com.android.soundrecorder",
                        "com.android.soundrecorder.RecordActivity", false);
                break;
            case R.id.img_lockscreen_camera:
                //mKeyguardManager.launchCamera();
                break;
        }
    }

    private void launchFlashLight() {
        boolean torchState = Settings.System.getInt(mContext.getContentResolver(),
                ExtraSettings.System.TORCH_STATE,
                0) == 0 ? false : true;
        Intent intent = new Intent(ExtraIntent.ACTION_TOGGLE_TORCH);
        if (torchState) {
            intent.putExtra(ExtraIntent.EXTRA_IS_ENABLE, false);
        } else {
            intent.putExtra(ExtraIntent.EXTRA_IS_ENABLE, true);
        }

        if (isOpen == torchState) {
            isOpen = !isOpen;
            mContext.sendBroadcast(intent);
        }
    }

    private void launchActivity(String packageName, String className, boolean secure) {
        LogUtil.d("launchActivity()--------> packageName:" + packageName + ", className:" + className + ", secure:" + secure + ", isLockPatternEnabled:" + mLockPatternUtils.isLockPatternEnabled());
        if (!mLockPatternUtils.isLockPatternEnabled() && !mLockPatternUtils.isLockPasswordEnabled() && !mLockPatternUtils.isBiometricWeakInstalled()) {
            LogUtil.d("launchActivity()--------> secure set false ");
            secure = false;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(packageName, className));
        //intent.setClassName(packageName, className);
        if (secure) {
            intent.setAction(Constant.ACTION_LAUNCH_SECURE_APP);
        }
        ActivityOptions options = ActivityOptions.makeCustomAnimation(mContext, R.anim.in_from_bottom, R.anim.out_to_top);
        mKeyguardManager.launchActivity(intent, options.toBundle(), secure);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        RecommendAppInfo item = (RecommendAppInfo)
                (arg0.getAdapter().getItem(arg2));
        LogUtil.d("onItemClick ------------> position:" + arg0.getAdapter().getItem(arg2));

        switch (item.type) {
            case 0:
                // download apk
                break;
            case 1:
                // intent app
                launchActivity(item.packageName, item.className, false);
                break;
            case 2:
                // to appmanager activity
                launchActivity("com.lewa.lockscreen2", "com.lewa.lockscreen2.LockscreenAppManager", false);
                break;
        }
    }

    private class BackgroundTask extends AsyncTask<String, Integer, String> {

        private Bitmap mBitmap;
        private String mPath;
        private Context mContext;

        public BackgroundTask(String path, Context context) {
            LogUtil.d("BackgroundTask ------------>");
            this.mPath = path;
            mContext = context;

        }

        @Override
        protected void onPreExecute() {
            LogUtil.d("BackgroundTask onPreExecute ------------>");

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            LogUtil.d("BackgroundTask doInBackground ------------>");
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                mBitmap = BitmapFactory.decodeFile(mPath, options);//decodeResource(getResources(), res);
                if (options.outWidth > Constant.MAX_PIC_WIDTH) {
                    options.outHeight = options.outHeight * Constant.MAX_PIC_WIDTH / options.outWidth;
                    options.inSampleSize = options.outWidth / Constant.MAX_PIC_WIDTH;
                    options.outWidth = Constant.MAX_PIC_WIDTH;
                    options.inJustDecodeBounds = false;
                    mBitmap = BitmapFactory.decodeFile(mPath, options);
                } else {
                    mBitmap = BitmapFactory.decodeFile(mPath);
                }
            } catch (Exception e) {
                LogUtil.d("BackgroundTask doInBackground ------------> error:"
                        + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            LogUtil.d("BackgroundTask onPostExecute ------------>");
            if (mBitmap != null){
                Drawable drawable = new BitmapDrawable(mContext.getResources(),
                        mBitmap);
                LogUtil.d("refreshPaperResource onPostExecute ------------>" + mPath);
                refreshPaperDrawable(drawable);
            }
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            LogUtil.d("UpdateReceiver onReceive()--------> action:" + action);
            if (Intent.ACTION_TIME_TICK.equals(action)) {
                if (LockscreenLayout.this.getVisibility() == View.VISIBLE) {
                    updateTimeAndData();
                }
            }

            if (Constant.WALLPAPER_ROTATION_TYPE_ACTION.equals(action)) {
                wallpaperRotation = intent.getIntExtra("rotation_type_key", Constant.WallpaperRotation.SCREEN_ON.ordinal());

                LogUtil.d("onReceive ----------> WALLPAPER_ROTATION_TYPE_ACTION wallpaperRotation:" + wallpaperRotation);
            } else if (Constant.NETWORK_DOWNLOAD_TYPE_ACTION.equals(action)) {
                mDownloadType = intent.getIntExtra("download_type_key", Constant.DownloadType.WIFI_UPDATE.ordinal());
                LogUtil.d("onReceive ----------> NETWORK_DOWNLOAD_TYPE_ACTION mDownloadType:" + mDownloadType);
            }

            if (Constant.APP_MANAGER_ACTION.equals(action)) {
                mLocalAppInfo = intent.getStringExtra("app_manager_value");
                LogUtil.d("onReceive ---------> mRecommendsList:" + mRecommendsList.getClass());
                notifyDataSetChanged();
            }

            if (Constant.REFRESH_DATA_ACTION.equals(action)) {
                mHttpRequest.updateImageAlways(mWidth, mHeight, null);
            } else if (Constant.REFRESH_DATA_COMPLETE_ACTION.equals(action)) {
                mWallpaperDownloadExpires = Settings.System.getLong(mContext.getContentResolver(), Constant.WALLPAPER_DOWNLOAD_EXPIRES, 0);
                mWallpaperPath = FileUtils.getWallpaperPath();
                notifyDataSetChanged();
            }
        }
    }

    private class TorchChangeObserver extends ContentObserver {

        public TorchChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            LogUtil.d("TorchChangeObserver onChange -------------> ");

            isOpen = Settings.System.getInt(mContext.getContentResolver(),
                    ExtraSettings.System.TORCH_STATE,
                    0) == 0 ? false : true;
            if (isOpen) {
                mHandler.sendMessage(mHandler.obtainMessage(2, 1, 0));
            } else {
                mHandler.sendMessage(mHandler.obtainMessage(2, 0, 0));
            }
        }
    }

    private class DownloadsChangeObserver extends ContentObserver {

        private DownloadsChangeObserver(Handler handler, Context context) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            if (System.currentTimeMillis() - lastUpdateDownloadTime < 200 || mHandleView.isDragging()) {
                return;
            }
            lastUpdateDownloadTime = System.currentTimeMillis();
            Set<Map.Entry<Long, RecommendAppInfo>> sets = mDownloadMap.entrySet();
            LogUtil.d("onChange -------------> size:" + sets.size());
            synchronized (mDownloadViewMap) {
                for (Map.Entry<Long, RecommendAppInfo> entry : sets) {
                    if (!mDownloadMap.containsKey(entry.getKey())) {
                        continue;
                    }
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putLong("downloadId", entry.getKey());
                    msg.obj = bundle;
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 1:
                LogUtil.d("handleMessage -------------> downloadId:" + ((Bundle) msg.obj).getLong("downloadId"));
                    Bundle bundle = (Bundle) msg.obj;
                    updateStatus(bundle.getLong("downloadId"));
                    break;
                case 2:
                    if (msg.arg1 == 1) {
                        mImgFlashlight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.flashlight_pressed));
                    } else {
                        mImgFlashlight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.flashlight));
                    }
                    break;
            }
        }
    };

    private void setIconPress(View view, boolean isPress) {
        LogUtil.d("setIconPress -------------> " + isPress);
        if (!(view instanceof RecommendAppLayout)) {
            return;
        }
        ((RecommendAppLayout) view).setDrawableChanged(isPress);
        ImageView imageView = (ImageView) view.findViewById(R.id.img_icon);
        Drawable drawable = imageView.getDrawable();
        if (isPress) {
            drawable.setColorFilter(0xb3000000, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.clearColorFilter();
        }
        imageView.setImageDrawable(drawable);
        view.invalidate();
    }

    private synchronized void updateStatus(long downloadId) {
        if (!mDownloadMap.containsKey(downloadId)) {
            LogUtil.d("updateStatus -------------> mDownloadMap is not contains downloadId");
            return;
        }
        int status = mLewaDownloadManager.getStatusById(downloadId);
        //LogUtil.d("updateStatus() -----------> status:" + status);
        View v = mDownloadViewMap.get(downloadId);
        ProgressBar bar = null;
        ImageView imgPause = null;
        if (v != null) {
            bar = (ProgressBar) v.findViewById(R.id.download_progress);
            imgPause = (ImageView) v.findViewById(R.id.img_pause);
        }
        switch (status) {
            case LewaDownloadManager.STATUS_RUNNING:
                if (bar != null && imgPause != null) {
                    bar.setVisibility(View.VISIBLE);
                    imgPause.setVisibility(View.INVISIBLE);
                }
                int currentByte[] = mLewaDownloadManager.getDownloadBytes(downloadId);
                //LogUtil.d("updateStatus() -----------> currentByte:" + currentByte[0] + " totol:" + currentByte[1]);
                if (currentByte[1] != 0) {
                    int progress = (int) (100.0f * currentByte[0] / currentByte[1]);
                    if (progress <= 100 && progress >= 0 && bar != null) {
                        bar.setProgress(progress);
                        LogUtil.d("updateStatus() -----------> progress:" + progress + ",  " + downloadId);
                    }
                }
                if (v != null) {
                    v.invalidate();
                }
                break;
            case LewaDownloadManager.STATUS_PAUSED:
                LogUtil.d("updateStatus() -----------> STATUS_PAUSED , " + downloadId);
                if (bar != null && imgPause != null) {
                    bar.setVisibility(View.VISIBLE);
                    imgPause.setVisibility(View.VISIBLE);
                }
                break;
            case LewaDownloadManager.STATUS_PENDING:
                LogUtil.d("updateStatus() -----------> STATUS_PENDING , " + downloadId);
                break;
            case LewaDownloadManager.STATUS_FAILED:
                LogUtil.d("updateStatus() -----------> STATUS_FAILED");
                if (bar != null && imgPause != null) {
                    bar.setVisibility(View.INVISIBLE);
                    imgPause.setVisibility(View.INVISIBLE);
                }
                mDownloadMap.remove(downloadId);
                break;
            case LewaDownloadManager.STATUS_SUCCESSFUL:
                LogUtil.d("updateStatus() -----------> STATUS_SUCCESSFUL");
                LogUtil.d("updateStatus() -----------> " + mDownloadMap.get(downloadId));
                if (bar != null && imgPause != null) {
                    bar.setVisibility(View.INVISIBLE);
                    imgPause.setVisibility(View.INVISIBLE);
                }
                new InstallTask(downloadId).execute();
                break;
        }
        mLastUpdateDownloadTime = System.currentTimeMillis();
    }

    private void initNotiExtras() {
        LewaDownloadManager.Query query = new LewaDownloadManager.Query()
                .setFilterByStatus(LewaDownloadManager.STATUS_PAUSED | LewaDownloadManager.STATUS_RUNNING | LewaDownloadManager.STATUS_PENDING);
        Cursor cursor = null;
        try {
            cursor = mLewaDownloadManager.query(query);
            if (cursor == null) {
                return ;
            }

            String notiExtras;
            Long downloadId;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                notiExtras = cursor.getString(cursor.getColumnIndexOrThrow("notificationextras"));
                downloadId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                mDownloadFirstMap.put(downloadId, notiExtras);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class InstallTask extends AsyncTask<String, Integer, Boolean> {

        private long mDownloadId;

        public InstallTask(long downloadId) {
            LogUtil.d("InstallTask ------------>");
            this.mDownloadId = downloadId;
        }

        @Override
        protected void onPreExecute() {
            LogUtil.d("InstallTask onPreExecute ------------>");
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean isSuccess = false;
            final List<String> path = mLewaDownloadManager.getDownloadFilePath(mDownloadId);
            if (path != null && path.size() > 0) {
                LogUtil.d("doInBackground() -----------> STATUS_SUCCESSFUL path:" + path.get(0));
                isSuccess = FileUtils.installApk(path.get(0));
            } else {
                LogUtil.d("doInBackground() -----------> STATUS_SUCCESSFUL installApk fail, path = null or size is 0");
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            LogUtil.d("InstallTask onPostExecute ------------> b:" + b);
            if (b) {
                // delete db
                ContentValues values = new ContentValues();
                values.put("isInstalled", 1);
                if (mDownloadMap.get(mDownloadId) != null) {
                    String packageName = mDownloadMap.get(mDownloadId).packageName;
                    mDbUtil.update(values, " package_name=? ", new String[]{packageName});
                    mDownloadMap.remove(mDownloadId);
                    mDownloadViewMap.remove(mDownloadId);
                    if (mLewaDownloadManager != null) {
                        final List<String> path = mLewaDownloadManager.getDownloadFilePath(mDownloadId);
                        mLewaDownloadManager.remove(mDownloadId);
                    }
                    appCommendChanged();
                    // updateData
                    if (mRecommendAppSize <= Constant.MIN_RECOMMENDAPP_COUNT){
                        LogUtil.d("mRecommendAppSize <= 5, to updateImage");
                        mHttpRequest.updateImage(mDownloadType, mWidth, mHeight,  Settings.System.getString(mContext.getContentResolver(), Constant.RECOMMENDAPP_URL));
                    }
                } else {
                    mDownloadMap.remove(mDownloadId);
                    mDownloadViewMap.remove(mDownloadId);
                    if (mLewaDownloadManager != null) {
                        mLewaDownloadManager.remove(mDownloadId);
                    }
                    sBBSDownloadId = -1;
                }
            } else {
                mDownloadMap.remove(mDownloadId);
                mDownloadViewMap.remove(mDownloadId);
            }
        }
    }

    private class ProgressUpdateListener implements OnClickListener {

        private TextView mTextView;
        private String apkName;
        private RecommendAppInfo info;

        public ProgressUpdateListener(TextView textView, RecommendAppInfo info) {
            this.mTextView = textView;
            this.info = info;
        }

        private long isDownloading() {
            Set<Map.Entry<Long, RecommendAppInfo>> sets = mDownloadMap.entrySet();
            for (Map.Entry<Long, RecommendAppInfo> entry : sets) {
                if (entry.getValue().packageName.equals(info.packageName)) {
                    long downloadId = entry.getKey();
                    return downloadId;
                }
            }
            return -1;
        }

        @Override
        public void onClick(View view) {
            long downloadId = isDownloading();
            if (downloadId != -1) {
                LogUtil.d("ProgressUpdateListener  onClick -------------> " + info.packageName + " ---> downloading");

                ImageView imgPause = (ImageView) view.findViewById(R.id.img_pause);
                switch (mLewaDownloadManager.getStatusById(downloadId)) {
                    case LewaDownloadManager.STATUS_PAUSED:
                        LogUtil.d("ProgressUpdateListener  onClick -------------> STATUS_PAUSED, to download , " + downloadId);
                        imgPause.setVisibility(View.INVISIBLE);
                        mLewaDownloadManager.resumeDownload(downloadId);
                        break;
                    case LewaDownloadManager.STATUS_RUNNING:
                        LogUtil.d("ProgressUpdateListener  onClick -------------> STATUS_RUNNING, to pause , " + downloadId);
                        imgPause.setVisibility(View.VISIBLE);
                        mLewaDownloadManager.pauseDownload(downloadId);
                        break;
                    case LewaDownloadManager.STATUS_PENDING:
                        LogUtil.d("ProgressUpdateListener  onClick -------------> STATUS_PENDING, to pause , " + downloadId);
                        mLewaDownloadManager.pauseDownload(downloadId);
                        break;
                }
                mDownloadMap.remove(downloadId);
                mDownloadViewMap.remove(downloadId);
                mDownloadMap.put(downloadId, info);
                mDownloadViewMap.put(downloadId, view);
                return;
            }

            //apkName = UUID.randomUUID().toString() + ".apk";

            LewaDownloadManager.Request request = new LewaDownloadManager.Request(Uri.parse(info.url));
            try {
                request.setDestinationInExternalPublicDir(Constant.RECOMMENDAPP_APP, info.name + ".apk");
            } catch (Exception e) {
                LogUtil.e("setDestinationInExternalPublicDir ---------> error:" + e.getMessage());
                e.printStackTrace();
            }
            request.setNotiExtras(info.packageName);
            request.setMimeType("application/vnd.android.package-archive");
            request.setShowRunningNotification(true);
            request.setNotificationVisibility(LewaDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI);
            downloadId = mLewaDownloadManager.enqueue(request);
            if (downloadId == -1) {
                // fail
            } else {
                mDownloadMap.put(downloadId, info);
                mDownloadViewMap.put(downloadId, view);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            LogUtil.d("dispatchKeyEvent -----------> keycode:" + event.getKeyCode());
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_HOME:
                    if (mHandleView.isOpened()) {
                        mHandleView.closeHandle();
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private class AppAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private PackageManager mPM;
        private List<RecommendAppInfo> data = new ArrayList<RecommendAppInfo>();

        private static final int COLUMN_NUM = 4;

        public AppAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mPM = mContext.getPackageManager();
        }

        public void setData(List<RecommendAppInfo> data) {
            this.data.clear();
            this.data.addAll(data);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private View getDownloadingView(int position) {
            Set<Map.Entry<Long, RecommendAppInfo>> sets = mDownloadMap.entrySet();
            for (Map.Entry<Long, RecommendAppInfo> entry : sets) {
                if (entry.getValue().packageName.equals(data.get(position).packageName)) {
                    LogUtil.d("getDownloadingView -------------> " + entry.getValue().packageName);
                    return mDownloadViewMap.get(entry.getKey());
                }
            }
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            LogUtil.d("getView ----------> position:" + position );
            ViewHolder holder;

            int type = data.get(position).type;
            if (convertView == null || type == 2) {
                holder = new ViewHolder();
                convertView = getDownloadingView(position);

                if (convertView == null || type == 2) {
                    convertView = mInflater.inflate(R.layout.recommendapp_icon,
                            null);
                }

                holder.appIcon = (ImageView) convertView
                        .findViewById(R.id.img_icon);
                holder.name = (TextView) convertView
                        .findViewById(R.id.txt_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (type == 0) {
                try {
                    holder.name.setText(data.get(position).name);
                    mBitmapCache.setImageBitmap(holder.appIcon, data.get(position).iconName);
                } catch (Exception e) {
                    LogUtil.d("getView ----------> packageName:" + data.get(position).packageName
                            + ", e:" + e.getMessage());
                    e.printStackTrace();
                }

                Set<Map.Entry<Long, String>> firstSets = mDownloadFirstMap.entrySet();
                if (mDownloadFirstMap.size() > 0){
                    Long[] downloadIds = new Long[firstSets.size()];
                    int i = 0;
                    for (Map.Entry<Long, String> entry : firstSets) {
                        LogUtil.d("isDownLoding:" + entry.getValue() + ", packageName;" + data.get(position).packageName);
                        if (entry.getValue().contains(data.get(position).packageName)) {
                            mDownloadMap.remove(entry.getKey());
                            mDownloadViewMap.remove(entry.getKey());
                            mDownloadMap.put(entry.getKey(), data.get(position));
                            mDownloadViewMap.put(entry.getKey(), convertView);
                            downloadIds[i] = entry.getKey();
                            i++;
                        }
                    }
                    
                    for(int j = 0, L = downloadIds.length; j < L; j++){
                        mDownloadFirstMap.remove(downloadIds[j]);
                    }
                }

                convertView.setOnClickListener(new ProgressUpdateListener(holder.name, data.get(position)));
            } else if (type == 1) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName(data.get(position).packageName,
                            data.get(position).className);
                    LogUtil.d("getView ----------> packageName:" + data.get(position).packageName
                    		+ ", className:" + data.get(position).className);
                    android.content.pm.ResolveInfo resolveInfo = mPM.resolveActivity(intent, 0);

                    holder.name.setText(resolveInfo.loadLabel(mPM));
                    mBitmapCache.setLocalImageBitmap(holder.appIcon, resolveInfo);
                } catch (Exception e) {
                    LogUtil.d("getView ----------> packageName:" + data.get(position).packageName
                            + ", e:" + e.getMessage());
                    Bitmap bitmap = BitmapFactory.decodeResource(mLockScreenContext.getResources(), R.drawable.recommendappicon);
                    holder.appIcon.setImageDrawable(ThemeManager.getCustomizedIcon(new BitmapDrawable(bitmap)));
                    e.printStackTrace();
                }
            } else if (type == 2) {
//                holder.appIcon.setImageDrawable(ThemeManager.getCustomizedIcon(getResources().getDrawable(R.drawable.recommend_more)));
                holder.appIcon.setImageResource(R.drawable.recommend_more);
                holder.name.setText(data.get(position).name);
            }
            return convertView;
        }
    }

    class ViewHolder {
        public ImageView appIcon;
        public TextView name;
    }
}
