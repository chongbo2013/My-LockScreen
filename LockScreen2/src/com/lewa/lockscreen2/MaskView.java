package com.lewa.lockscreen2;

/**
 * Created by lewa on 3/25/15.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.graphics.Shader;
import android.view.View;
import android.view.WindowManager;
import com.lewa.lockscreen2.util.LogUtil;

public class MaskView extends View {

    private LinearGradient linearGradient = null;
    private Paint paint = null;
    private Context mContext;
    private int mWidth;
    private int mHeight;

    public MaskView(Context context) {
        super(context);
    }

    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWidth = wm.getDefaultDisplay().getWidth();
        mHeight = wm.getDefaultDisplay().getHeight();
        linearGradient = new LinearGradient(mWidth, mHeight, mWidth, mHeight - mContext.getResources().getDimension(R.dimen.mask_height), new int[]{
                mContext.getResources().getColor(R.color.mask_bg), Color.TRANSPARENT}, null,
                Shader.TileMode.CLAMP);
        paint = new Paint();
        LogUtil.d("onWindowFocusChanged -----------> " + wm.getDefaultDisplay().getWidth() + ", " + wm.getDefaultDisplay().getHeight());
    }

    public MaskView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setShader(linearGradient);
        canvas.drawRect(0, 0, mWidth, mHeight, paint);
    }

}