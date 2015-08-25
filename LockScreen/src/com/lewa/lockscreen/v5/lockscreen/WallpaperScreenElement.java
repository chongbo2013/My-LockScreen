
package com.lewa.lockscreen.v5.lockscreen;

import org.w3c.dom.Element;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.ScreenElementLoadException;
import com.lewa.lockscreen.laml.ScreenElementRoot;
import com.lewa.lockscreen.laml.data.VariableNames;
import com.lewa.lockscreen.laml.elements.ImageScreenElement;
import com.lewa.lockscreen.laml.util.Utils;

public class WallpaperScreenElement extends ImageScreenElement {

    public static final String TAG_NAME = "Wallpaper";

    public static final String LOG_TAG = TAG_NAME;

    private RectF mRect;

    private boolean mFill = true;

    public WallpaperScreenElement(Element node, ScreenElementRoot root)
            throws ScreenElementLoadException {
        super(node, root);
        String attr = node.getAttribute("fill");
        if (!TextUtils.isEmpty(attr))
            mFill = Boolean.parseBoolean(attr);
    }

    @Override
    public float getHeight() {
        return mAni.getHeight();
    }

    @Override
    public float getMaxHeight() {
        return mAni.getMaxHeight();
    }

    @Override
    public float getMaxWidth() {
        return mAni.getMaxWidth();
    }

    @Override
    public float getWidth() {
        return mAni.getWidth();
    }

    @Override
    public void init() {
        super.init();
        BitmapDrawable drawable = (BitmapDrawable) ThemeResources
                .getLockWallpaperCache(getContext().getContext());
        if (drawable != null) {
            Bitmap bmp = drawable.getBitmap();
            if (bmp != null && bmp.getWidth() > 1 && mBlurRadius > 0) {
                mBitmap = getBlurredBitmap(bmp);
            } else {
                mBitmap = bmp;
            }
        }
        if (mFill) {
            float width = getWidth();
            if (width < 0)
                width = scale(Utils.getVariableNumber(VariableNames.SCREEN_WIDTH, getVariables()));
            float height = getHeight();
            if (height < 0)
                height = scale(Utils.getVariableNumber(VariableNames.SCREEN_HEIGHT, getVariables()));
            mRect = new RectF(0, 0, width, height);
        }
    }

    @Override
    public void doRender(Canvas c) {
        if (mFill) {
            Bitmap bmp = mCurrentBitmap;
            if (bmp != null) {
                c.drawBitmap(bmp, null, mRect, mPaint);
            }
        } else {
            super.doRender(c);
        }
    }
}
