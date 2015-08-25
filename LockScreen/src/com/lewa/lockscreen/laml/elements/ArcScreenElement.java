package com.lewa.lockscreen.laml.elements;

import org.w3c.dom.Element;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.lewa.lockscreen.laml.ScreenElementLoadException;
import com.lewa.lockscreen.laml.ScreenElementRoot;
import com.lewa.lockscreen.laml.data.Expression;

/**
 * ArcScreenElement.java:
 * 
 * @author yljiang@lewatek.com 2014-7-8
 */
public class ArcScreenElement extends GeometryScreenElement {

    public static final String TAG_NAME = "Arc";
    private float              mAngle;
    private Expression         mAngleExp;
    private boolean            mClose;
    private float              mSweep;
    private Expression         mSweepExp;

    public ArcScreenElement(Element ele, ScreenElementRoot root) throws ScreenElementLoadException{
        super(ele, root);
        mAngleExp = Expression.build(ele.getAttribute("startAngle"));
        mSweepExp = Expression.build(ele.getAttribute("sweep"));
        mClose = Boolean.parseBoolean(ele.getAttribute("close"));
    }

    public void tick(long currentTime) {
        super.tick(currentTime);
        if (!isVisible()) 
            return;
        mAngle = (float)mAngleExp.evaluate(mRoot.getVariables());
        mSweep = (float)mSweepExp.evaluate(mRoot.getVariables());
    }

    protected void onDraw(Canvas canvas, DrawMode mode) {
        float width = getWidth();
        float height = getHeight();
        float left = getX() - width / 2;
        float top = getY() - height / 2;
        canvas.drawArc(new RectF(left, top, left + width, top + height), mAngle, mSweep, mClose, mPaint);
    }
}
