package com.lewa.lockscreen2;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.lewa.lockscreen2.util.LogUtil;

/**
 * Created by lewa on 4/8/15.
 */
public class RecommendAppLayout extends RelativeLayout {

    private ImageView mIcon;

    private boolean isDrawableChanged;

    public RecommendAppLayout(Context context) {
        super(context);
    }

    public RecommendAppLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecommendAppLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDrawableChanged(boolean isDrawableChanged){
        this.isDrawableChanged = isDrawableChanged;
    }

    @Override
    protected void onFinishInflate() {
        mIcon = (ImageView) findViewById(R.id.img_icon);
        super.onFinishInflate();
    }

    @Override
    protected void drawableStateChanged() {
        if (isDrawableChanged){
            return;
        }
        LogUtil.d("drawableStateChanged -----------> " + isPressed());
        Drawable drawable = mIcon.getDrawable();
        LogUtil.d("drawableStateChanged -----------> " + drawable);
        if (isPressed()){
            drawable.setColorFilter(0xb3000000, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.clearColorFilter();
        }
        mIcon.setImageDrawable(drawable);
        invalidate();
        super.drawableStateChanged();
    }
}
