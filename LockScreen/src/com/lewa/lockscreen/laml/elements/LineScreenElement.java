package com.lewa.lockscreen.laml.elements;

import org.w3c.dom.Element;

import android.graphics.Canvas;

import com.lewa.lockscreen.laml.ScreenElementLoadException;
import com.lewa.lockscreen.laml.ScreenElementRoot;
import com.lewa.lockscreen.laml.data.Expression;

/**
 * LineScreenElement.java:
 * 
 * @author yljiang@lewatek.com 2014-7-8
 */
public class LineScreenElement extends GeometryScreenElement {

    public static final String TAG_NAME = "Line";
    private Expression         mEndXExp;
    private Expression         mEndYExp;

    public LineScreenElement(Element ele, ScreenElementRoot root) throws ScreenElementLoadException{
        super(ele, root);
        mEndXExp = Expression.build(ele.getAttribute("x1"));
        mEndYExp = Expression.build(ele.getAttribute("y1"));
    }

    private final float getEndX() {
        return mEndXExp != null ? scale(mEndXExp.evaluate(getContext().mVariables)) : 0;
    }

    private final float getEndY() {
        return mEndYExp != null ? scale(mEndYExp.evaluate(getContext().mVariables)) : 0;
    }

    protected void onDraw(Canvas canvas, DrawMode mode) {
        canvas.drawLine(getX(), getY(), getEndX(), getEndY() , mPaint);
//        canvas.drawLine(getX(), getY(), getEndX() - getX(), getEndY() - getY(), mPaint);
    }
}
