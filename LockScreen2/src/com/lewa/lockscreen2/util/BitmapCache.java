package com.lewa.lockscreen2.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.lewa.lockscreen2.R;
import com.lewa.themes.ThemeManager;
import com.lewa.themes.resource.IconCustomizer;

/**
 * Created by lewa on 3/12/15.
 */
public class BitmapCache {

    private static HashMap<String, SoftReference<Drawable>> mCache = new HashMap<String, SoftReference<Drawable>>();
    private static BitmapCache instance;

    private static Context mContext;
    private static PackageManager mPM;
    private static int mIconDpi;

    private BitmapCache() {

    }

    public static BitmapCache getInstance(Context context) {
        if (instance == null) {
            instance = new BitmapCache();
            mContext = context;
            mPM = mContext.getPackageManager();

            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            mIconDpi = activityManager.getLauncherLargeIconDensity();
        }
        return instance;
    }

    public void setImageBitmap(ImageView imageView, String fileName) {
        if (mCache.containsKey(fileName)) {
            SoftReference<Drawable> reference = mCache.get(fileName);
            Drawable drawable = reference.get();
            if (drawable != null) {
                imageView.setImageDrawable(drawable);
                return;
            }
        }
        new BackgroundTask(imageView, fileName).execute();
    }

    public void setLocalImageBitmap(ImageView imageView, ResolveInfo resolveInfo) {
        /*if (mCache.containsKey(resolveInfo.activityInfo.packageName)) {
            SoftReference<Drawable> reference = mCache.get(resolveInfo.activityInfo.packageName);
            Drawable drawable = reference.get();
            if (drawable != null) {
                imageView.setImageDrawable(drawable);
                return;
            }
        }
        new CustomizedIconTask(imageView, resolveInfo).execute();*/
    	Drawable drawable = null;
        try {
        	drawable = new FastBitmapDrawable(getIcon(resolveInfo));
        } catch (Exception e) {
        	drawable = lewa.content.res.IconCustomizer.getCustomizedIcon(mContext, "", "", mContext.getResources().getDrawable(R.drawable.recommendappicon));
            e.printStackTrace();
        }
        imageView.setImageDrawable(drawable);
    }

    public void clear() {
        mCache.clear();
    }

    public void setCacheSize(int size) {

    }

    private class BackgroundTask extends AsyncTask<String, Integer, Boolean> {

        private ImageView mImageView;
        private String mFileName;
        private Drawable mDrawable;

        public BackgroundTask(ImageView imageView, String fileName) {
//            LogUtil.d("BackgroundTask ------------>");
            this.mImageView = imageView;
            this.mFileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
//            LogUtil.d("BackgroundTask doInBackground ------------>");
            try {
                Bitmap bitmap;
                File file = new File(Constant.RECOMMENDAPP_ICON_PATH + File.separator + mFileName);
                if (!file.exists()) {
                    LogUtil.d("BackgroundTask doInBackground ------------>  recommendapp icon is not exists ");
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.recommendappicon);
                } else {
                    bitmap = BitmapFactory.decodeFile(Constant.RECOMMENDAPP_ICON_PATH + File.separator + mFileName);
                }

                mDrawable = ThemeManager.getCustomizedIcon(new BitmapDrawable(mContext.getResources(), getScaleBitmap(bitmap)));

                if (mDrawable != null) {
                    mCache.put(mFileName, new SoftReference<Drawable>(mDrawable));
                    return true;
                }
            } catch (Exception e) {
                LogUtil.d("BackgroundTask doInBackground ------------> error:"
                        + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
//            LogUtil.d("BackgroundTask onPostExecute ------------> b:" + b);
            if (mDrawable != null) {
                mImageView.setImageDrawable(mDrawable);
            }
        }
    }

//    private class CustomizedIconTask extends AsyncTask<String, Integer, Boolean> {
//
//        private ImageView mImageView;
//        private ResolveInfo mResolveInfo;
//        private Drawable mDrawable;
//
//        public CustomizedIconTask(ImageView imageView, ResolveInfo resolveInfo) {
//            this.mImageView = imageView;
//            this.mResolveInfo = resolveInfo;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            LogUtil.d("CustomizedIconTask doInBackground ------------> mContext:" + mContext);
//            try {
////                mDrawable = new FastBitmapDrawable(getIcon(mResolveInfo));
//                mDrawable = ThemeManager.getInstance(mContext).loadIcon(mResolveInfo);
//                if (mDrawable != null) {
//                    mCache.put(mResolveInfo.activityInfo.packageName, new SoftReference<Drawable>(mDrawable));
//                    return true;
//                }
//            } catch (Exception e) {
//                LogUtil.d("CustomizedIconTask doInBackground ------------> error:" + e.getMessage());
//                mDrawable = lewa.content.res.IconCustomizer.getCustomizedIcon(mContext, "", "", mContext.getResources().getDrawable(R.drawable.recommendappicon));
//                e.printStackTrace();
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean b) {
//            if (mDrawable != null) {
//                mImageView.setImageDrawable(mDrawable);
//            }
//        }
//    }


    private Bitmap getScaleBitmap(Bitmap orignal) {
        if (orignal == null || orignal.getWidth() <= 0 || orignal.getHeight() <= 0) {
            return null;
        }

        Bitmap recommendBitmapTemp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.recommend_icon);

        Bitmap bitmap = Bitmap.createBitmap(orignal.getWidth(), orignal.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(orignal, new Matrix(), null);
        Matrix recommendMatrix = new Matrix();
        recommendMatrix.postScale((float) orignal.getWidth() / recommendBitmapTemp.getWidth(), (float) orignal.getHeight() / recommendBitmapTemp.getHeight());
        Bitmap recommendBitmap = Bitmap.createBitmap(recommendBitmapTemp, 0, 0, recommendBitmapTemp.getWidth(), recommendBitmapTemp.getHeight(), recommendMatrix, true);

//            LogUtil.d("getScaleBitmap -----------> " + orignal.getWidth() + ", " + recommendBitmap.getWidth());
        canvas.drawBitmap(recommendBitmap, 0, 0, null);


        Matrix matrix = new Matrix();
        float scaleX = (float) IconCustomizer.sCustomizedIconWidth / (float) bitmap.getWidth();
        float scaleY = (float) IconCustomizer.sCustomizedIconHeight / (float) bitmap.getHeight();
        matrix.postScale(scaleX, scaleY);
        Bitmap iconBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return iconBitmap;
    }

    public Drawable loadIcon(ResolveInfo resolveInfo){
        return ThemeManager.getInstance(mContext).loadIcon(resolveInfo);
    }

    public Bitmap getIcon(ResolveInfo resolveInfo) {
        if (resolveInfo == null) {
            return makeDefaultIcon();
        }

        Bitmap bitmap;

        if (resolveInfo.activityInfo != null
                && resolveInfo.activityInfo.applicationInfo != null
                && (resolveInfo.icon == resolveInfo.activityInfo.applicationInfo.icon
                || (resolveInfo.icon == 0 && resolveInfo.activityInfo.icon == resolveInfo.activityInfo.applicationInfo.icon)
                || (resolveInfo.icon == 0 && resolveInfo.activityInfo.icon == 0))) {
            BitmapDrawable bd = lewa.content.res.IconCustomizer.getCustomizedIcon(mContext, resolveInfo.activityInfo.applicationInfo);
            if (bd != null) {   // bd might be null
                bitmap = bd.getBitmap();
            } else {
                bitmap = lewa.content.res.IconCustomizer.getCustomizedIcon(mContext, resolveInfo).getBitmap();
            }
        } else {
            bitmap = lewa.content.res.IconCustomizer.getCustomizedIcon(mContext, resolveInfo).getBitmap();
        }

        if (bitmap == null) {
            try {
                Drawable d = resolveInfo.loadIcon(mPM);
                bitmap = ((BitmapDrawable) d).getBitmap();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    public Bitmap getIcon(String fileName){
        Bitmap bitmap;

        File file = new File(fileName);
        if (!file.exists()) {
            return makeDefaultIcon();
        }

        bitmap = lewa.content.res.IconCustomizer.getCustomizedIcon(mContext, fileName).getBitmap();
        return bitmap;
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }

    private Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    private Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }
        return (d != null) ? d : getFullResDefaultActivityIcon();
    }
}
