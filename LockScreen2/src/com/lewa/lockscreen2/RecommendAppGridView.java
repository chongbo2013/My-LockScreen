
package com.lewa.lockscreen2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class RecommendAppGridView extends GridView {

    public RecommendAppGridView(Context context) {
        super(context);
    }

    public RecommendAppGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecommendAppGridView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
