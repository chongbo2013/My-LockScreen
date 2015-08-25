
package com.lewa.lockscreen.v5.lockscreen.activity;

import java.util.ArrayList;
import java.util.List;

import lewa.util.ImageUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.FancyDrawable;
import com.lewa.lockscreen.laml.util.AppIconsHelper;
import com.lewa.lockscreen.util.SurfaceWrapper;

public class FancyIconActivity extends Activity {
    private static final boolean ADAPTER = false;

    List<Drawable> mDrawables;

    private Bitmap mBackground;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mBackground == null) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Bitmap shot = SurfaceWrapper.screenshot(Math.min(display.getWidth(), display.getHeight()) / 3,
                    Math.max(display.getWidth(), display.getHeight()) / 3);
            if (shot != null && shot.getWidth() > 1) {
                mBackground = Bitmap.createBitmap(shot.getWidth(), shot.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mBackground.eraseColor(0xff000000);
                ImageUtils.fastBlur(shot, mBackground, 5);
                shot.recycle();
            }
        }
        super.onCreate(savedInstanceState);
        final Context context = this;
        int size = (int) (context.getResources().getDimensionPixelSize(
                android.R.dimen.app_icon_size) * 1.5);
        mDrawables = getFancyDrawables();
        if (ADAPTER) {
            GridView grid = new GridView(context);
            grid.setColumnWidth(size);
            grid.setNumColumns(GridView.AUTO_FIT);
            grid.setAdapter(new IconAdapter(context, mDrawables));
            grid.setBackgroundDrawable(new ColorDrawable(0x55ffffff));
            setContentView(grid);
        } else {
            LinearLayout layout = new LinearLayout(this);
            layout.setBackgroundDrawable(new ColorDrawable(0x55ffffff));
            for (Drawable d : mDrawables) {
                ImageView v = new ImageView(context);
                v.setImageDrawable(d);
                v.setPadding(5, 5, 5, 5);
                layout.addView(v);
            }
            setContentView(layout);
        }
        getWindow().setBackgroundDrawable(
                mBackground == null ? getPatternBackground() : new BitmapDrawable(getResources(),
                        mBackground));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDrawables != null) {
            for (Drawable d : mDrawables) {
                if (d instanceof FancyDrawable) {
                    ((FancyDrawable) d).cleanUp();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    private List<Drawable> getFancyDrawables() {
        List<Drawable> ds = new ArrayList<Drawable>();
        try {
            for (String icon : ThemeResources.getSystem().getFancyIcons()) {
                Drawable drawable = AppIconsHelper.getIconDrawable(this, icon, null);
                if (drawable != null) {
                    ds.add(drawable);
                }
            }
        } catch (Exception e) {
        }
        return ds;
    }

    private Drawable getPatternBackground() {
        int side = (int) (getResources().getDisplayMetrics().density * 20);
        Bitmap bmp = Bitmap.createBitmap(side * 2, side * 2, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(0x10000000);
        c.drawRect(0, 0, side, side, p);
        c.drawRect(side, side, side * 2, side * 2, p);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        return drawable;
    }

    private static class IconAdapter extends BaseAdapter implements OnClickListener,
            Drawable.Callback {

        private List<Drawable> mInfos;

        private LayoutParams mLayoutParams;

        private int mIconSize;

        public IconAdapter(Context context, List<Drawable> drawables) {
            mInfos = drawables;
            mIconSize = (int) (context.getResources().getDimensionPixelSize(
                    android.R.dimen.app_icon_size) * 1.5);
            mLayoutParams = new LayoutParams(mIconSize, mIconSize);
        }

        @Override
        public int getCount() {
            return mInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return mInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mInfos.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            Context context = parent.getContext();
            final ImageView v;
            if (convertView != null) {
                v = (ImageView) convertView;
            } else {
                v = new ImageView(context);
                v.setLayoutParams(mLayoutParams);
                v.setOnClickListener(this);
                v.setPadding(5, 5, 5, 5);
            }
            Drawable d = mInfos.get(position);
            d.setCallback(this);
            v.setImageDrawable(d);
            return v;
        }

        @Override
        public void onClick(View v) {
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            notifyDataSetInvalidated();
        }
    }
}
