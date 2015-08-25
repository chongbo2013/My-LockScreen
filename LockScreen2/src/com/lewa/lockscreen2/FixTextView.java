package com.lewa.lockscreen2;

import com.lewa.lockscreen2.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
/**
 * fix length text view, when setTimeString not remeasure and relayout
 * now only support time and date
 *   <attr name="textSize" format="integer"/> attr support set text size with unit sp
 *   <attr name="textType" format="integer"/> 0: tiem; 1: date
 * @author zhengwei
 *
 */
public class FixTextView extends View{
    private static Typeface mTypeface1;
    private static Typeface mTypeface;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mTextColor = Color.WHITE;
    private String mTimeString = "";
    Rect mRect;
    public FixTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FixTextViewAttr);
        int size = ta.getInt(R.styleable.FixTextViewAttr_textSize, 10);
        int textType = ta.getInteger(R.styleable.FixTextViewAttr_textType, 0);
        ta.recycle();
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics()));
        if (textType == 1) {
            mTimeString = context.getString(R.string.default_data);
            if (mTypeface1 == null){
                mTypeface1 = Typeface.createFromAsset(getContext().getAssets(), "fonts/NotoSansCJKsc-Light.otf");
            }
            mPaint.setTypeface(mTypeface1);
        } else {
            mTimeString = context.getString(R.string.default_time);
            if (mTypeface == null){
                mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaLT25UltraLight.ttf");
            }
            mPaint.setTypeface(mTypeface);
        }
        mPaint.setShadowLayer(1, 1, 1, 0x88000000);
        mRect = new Rect();
        StringBuffer sb = new StringBuffer(mTimeString);
        if (textType == 1) {//as mounth have two char so we add two more char length
            sb.append("122");
            mPaint.getTextBounds(sb.toString(), 0, sb.length(), mRect);
        } else {//default length not show the last word, so we add one length
            sb.append("1");
            mPaint.getTextBounds(sb.toString(), 0, sb.length(), mRect);
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mRect.width(), mRect.height() + 30);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(mTimeString, 0, mRect.height() + 10, mPaint);
    }
    public int getTextColor() {
        return mTextColor;
    }
    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
        mPaint.setColor(mTextColor);
    }
    public String getTimeString() {
        return mTimeString;
    }
    /**
     * set the display time
     * @param mTime
     */
    public void setTimeString(String mTime) {
        if (mTime != null && !mTime.equals(mTimeString)) {
            this.mTimeString = mTime;
            postInvalidate();
        }
    }
}
