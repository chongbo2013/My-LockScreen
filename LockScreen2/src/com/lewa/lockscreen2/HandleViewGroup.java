package com.lewa.lockscreen2;

import com.lewa.lockscreen2.util.LogUtil;
import com.lewa.lockscreen2.util.DensityUtil;
import lewa.util.ImageUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
/**
 *impl drawer
 */
public class HandleViewGroup extends RelativeLayout implements Runnable {
    private static final boolean DEBUG_DRAWER = false;
    private Context mContext;
    private ScrollerCompat mScrollerCompat;
    private View mFollowerView;    //main view of follower
    private ImageView mBlurView;    //blur image view; default it is a child of FollowerView
    private View mContentView;    //content view is our view defined
    private int mMaxMove; //max move measure is the height of our self layout;
                             //-----handle----
                            //-----our view........---
                            //so max move is layout'height minus handle'height
    private int mFollowerHeight; //follower's height
    private int mContentHeight; //content's height
    private int mHeight; //self height
    

    private float mMaxVelocity;
    private float mMinVelocity; 
    private int mTouchSlop;   // Distance to travel before a drag may begin
    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;
    public static interface OnDrawerOpenListener {
        public void onDrawerOpened();
    }
    public static interface OnDrawerCloseListener {
        public void onDrawerClosed();
    }
    public static interface OnDrawerScrollListener {
        public void onScrollStarted();
        public void onScrollEnded();
    }
    public HandleViewGroup(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public HandleViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }
    private void init() {
        final ViewConfiguration vc = ViewConfiguration.get(mContext);
        mMaxVelocity = vc.getScaledMaximumFlingVelocity() / 1000.0f;
        mMinVelocity = vc.getScaledMinimumFlingVelocity() / 1000.0f;
        mTouchSlop = vc.getScaledTouchSlop();
    }
    private float mDownY = 0;
    private float mTransY = 0.0f;
    private float mLastDy = 0.0f;
    private float mCurDy = 0.0f;
    private VelocityTracker mVelocityTracker = null;
    private static final int STATE_CLOSED = 0,
                            STATE_READY = 1,
                            STATE_OPENDED = 2,
                            STATE_DRAGGING = 3,
                            STATE_SCROLLING_DOWN = 4,
                            STATE_SCROLLING_UP = 5;
    int mState = STATE_CLOSED;
    private boolean bOpened = false;
    public boolean isOpened() {
    	return bOpened || mState == STATE_SCROLLING_UP;
    }

    public boolean isDragging() {
        return mState == STATE_DRAGGING;
    }

    public boolean onTouchEvent(MotionEvent event){
        if (DEBUG_DRAWER)
            LogUtil.d("--- onTouchEvent--> " + event.getAction());
        super.onTouchEvent(event);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        float x = event.getRawX();
        float y = event.getRawY();
        switch(event.getAction()){
        case MotionEvent.ACTION_DOWN:
            if (mState != STATE_CLOSED && mState != STATE_OPENDED) {
                return false;
            }
            mForceStop = true;
            mVelocityTracker.clear();
            mVelocityTracker.addMovement(event);
            //if scroller is still running, stop it
            if(mScrollerCompat != null){
                if(!mScrollerCompat.isFinished()){
                    mScrollerCompat.abortAnimation();
                }
            }
            mDownY = y;
            mTransY = this.getTranslationY();
            if (mState == STATE_CLOSED) {
                readyOpen();
            }
            break ;
        case MotionEvent.ACTION_MOVE:
            mVelocityTracker.addMovement(event);
            mLastDy = mCurDy;
            mCurDy = y - mDownY;
            float tty = mTransY + mCurDy;
            if (DEBUG_DRAWER)
                LogUtil.d("tty:" + tty + " this_height:" + mHeight + " mMaxMove:" + mMaxMove + " mFollowerHeight:" + mFollowerHeight + "  touch slop:" + mTouchSlop + "  dy:" + mCurDy + "  last dy:" + mLastDy);
            if (tty >= 0) {
                tty = 0;
            } else if (Math.abs(tty) > (mMaxMove + 200)) {
                tty = -(mMaxMove + 200);
            }
            if (Math.abs(y - mDownY) > mTouchSlop) { //should larger touch slop to start move
                if (DEBUG_DRAWER)
                    LogUtil.d("start move");
                if(mScrollerCompat != null){
                    if(!mScrollerCompat.isFinished()){
                        mScrollerCompat.abortAnimation();
                    }
                }
                if (mState != STATE_READY && mState != STATE_DRAGGING && mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrollStarted();
                }
                mState = STATE_DRAGGING;
                this.setTranslationY(tty);
                mFollowerView.setTranslationY(mFollowerHeight + tty - mHeight);
                mContentView.setTranslationY(mFollowerHeight + tty - mHeight);
                mBlurView.offsetTopAndBottom((int) - (mFollowerHeight + tty - mHeight) - mBlurView.getTop());
                //mBlurView.scrollTo(0, (int) (mFollowerView.getTranslationY()));
            }
            break ;
        case MotionEvent.ACTION_UP:
            mVelocityTracker.computeCurrentVelocity(1);
            mVelocity = mVelocityTracker.getYVelocity();
            if (DEBUG_DRAWER)
                LogUtil.d("dvy" + mMinVelocity + " ty:" + getTranslationY() + ";mv:" + mVelocity);
            if (Math.abs(mVelocity) > mMinVelocity) {
                if (mCurDy - mLastDy > 0) {
                    if (DEBUG_DRAWER)
                        LogUtil.d("snap down");
                    closeHandle();
                } else {
                    if (DEBUG_DRAWER)
                        LogUtil.d("snap up; max" + mMaxVelocity);
                    mLastTick = System.nanoTime();
                    mForceStop = false;
                    mVelocity = mVelocity > 0 ? -mVelocity : mVelocity;
                    float p0 = getTranslationY() - (-mMaxMove);
                    if (p0 < 0) {// && mVelocity < -8) {
                        mVelocity = 0;
                    } else if (mVelocity < - mMaxVelocity) {
                        mVelocity = -mMaxVelocity;
                    }
                    openHandle();
                }
            } else if (mState == STATE_DRAGGING){
                if (Math.abs(getTranslationY()) > mMaxMove/2) {
                    mLastTick = System.nanoTime();
                    mForceStop = false;
                    openHandle();
                } else {
                    closeHandle();
                }
            } else if (mState == STATE_READY) {
                closeHandle();
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        
        return true ;
    }
    private static final float ZERO_VELOCITY = 0.001f;//velocity for end
    private static final float ZERO_LENGTH = 3f; //balence diff
    private static final float SPRING_K = 0.00055f;//strong for tanhuang
    private static final float DAMPING_K = 0.026f;//against for tanhuang
    private long mLastTick;
    private float mVelocity;
    private boolean mForceStop = false;
    @Override
    public void run() {
        if (mForceStop) {
            if (DEBUG_DRAWER)
                LogUtil.d("force stop anime");
            return;
        }
        // TODO Auto-generated method stub
        long tt = System.nanoTime();
        float dt= (tt- mLastTick) / 1000000.0f; /* millisecond */
        dt = dt > 20 ? 20 : dt;
        float p0 = this.getTranslationY(); /* Current position */
        float v0 = mVelocity; /* Current velocity */

        float len = p0 - (-mMaxMove);
        float as = -len * SPRING_K; /* Spring force */
        float ad = -mVelocity * DAMPING_K; /* Damping force */

        float v1 = v0 + (as + ad) * dt; /* New velocity */
        float dp = 0.5f * (v0 + v1) * dt; /* Translation Y */
        float p1 = p0 + dp; /* New position */
        mLastTick = tt;
        mVelocity = v1;
        if (DEBUG_DRAWER)
            LogUtil.d("v:" + mVelocity + ";len:" + len + "; dt=" + dt + "; v0=" + v0 + "; as=" + as + "; ad=" + ad + "; p1=" + p1 + " tt:" + mLastTick/1000000);
        if ((Math.abs(mVelocity) < ZERO_VELOCITY && Math.abs(len) < ZERO_LENGTH)) {
            if (DEBUG_DRAWER)
                LogUtil.d("stop anime");
            {
                p1 = -mMaxMove;
                this.setTranslationY(p1);
                mFollowerView.setTranslationY(mFollowerHeight + p1 - mHeight);
                mContentView.setTranslationY(mFollowerHeight + p1 - mHeight);
                mBlurView.offsetTopAndBottom((int) - (mFollowerHeight + p1 - mHeight) - mBlurView.getTop());
            }
            if (!mForceStop) {
                mState = STATE_OPENDED;
                bOpened = true;
                if (mOnDrawerOpenListener != null) {
                    mOnDrawerOpenListener.onDrawerOpened();
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrollEnded();
                }
            }
        } else {
            {
                this.setTranslationY(p1);
                mFollowerView.setTranslationY(mFollowerHeight + p1 - mHeight);
                mContentView.setTranslationY(mFollowerHeight + p1 - mHeight);
                //mBlurView.scrollTo(0, mFollowerHeight + ty - mHeight);
                mBlurView.offsetTopAndBottom((int) - (mFollowerHeight + p1 - mHeight) - mBlurView.getTop());
            }
            this.postOnAnimation(this);
        }
    }
    @Override
    public void computeScroll() {    
        // TODO Auto-generated method stub
        if (DEBUG_DRAWER)
            LogUtil.d("computeScroll");
        if (mScrollerCompat != null && mScrollerCompat.computeScrollOffset()) {
            int ty = mScrollerCompat.getCurrY();
            if (DEBUG_DRAWER)
                LogUtil.d(mScrollerCompat.getFinalY() + "======" + ty);
            if (ty > 0) {
                this.setTranslationY(0);
            } else {
                this.setTranslationY(ty);
            }
            mFollowerView.setTranslationY(mFollowerHeight + ty - mHeight);
            mContentView.setTranslationY(mFollowerHeight + ty - mHeight);
            mBlurView.offsetTopAndBottom((int) - (mFollowerHeight + ty - mHeight) - mBlurView.getTop());
            invalidate();
        } else {
            if (DEBUG_DRAWER)
                LogUtil.d("have done the scoller -----");
            if (mState == STATE_SCROLLING_DOWN) {
                mState = STATE_CLOSED;
                bOpened = false;
                if (mOnDrawerCloseListener != null) {
                    mOnDrawerCloseListener.onDrawerClosed();
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrollEnded();
                }
            }
        }
    }
    public boolean openHandle() {
        if (DEBUG_DRAWER)
            LogUtil.d("openHandle -----" + this.getTop() + "-----" + this.getHeight() + "----" + mMaxMove);
        if(mScrollerCompat != null){
            if(!mScrollerCompat.isFinished()){
                mScrollerCompat.abortAnimation();
            }
        }
        mState = STATE_SCROLLING_UP;
        postOnAnimation(this);
        return true;
    }
    public boolean openNoAnime() {
        if (DEBUG_DRAWER)
            LogUtil.d("openNoAnime");
        if(mScrollerCompat != null){
            if(!mScrollerCompat.isFinished()){
                mScrollerCompat.abortAnimation();
            }
        }
        this.setTranslationY(mMaxMove);
        mFollowerView.setTranslationY(mFollowerHeight - mMaxMove - mHeight);
        mContentView.setTranslationY(mFollowerHeight - mMaxMove - mHeight);
        mState = STATE_OPENDED;
        bOpened = true;
        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
        return true;
    }
    public boolean closeNoAnime() {
        mForceStop = true;
        if (DEBUG_DRAWER)
            LogUtil.d("closeNoAnime");
        if(mScrollerCompat != null){
            if(!mScrollerCompat.isFinished()){
                mScrollerCompat.abortAnimation();
            }
        }
        if (mState != STATE_CLOSED) {
            this.setTranslationY(0);
            mFollowerView.setTranslationY(mFollowerHeight);
            mContentView.setTranslationY(mFollowerHeight);
            mState = STATE_CLOSED;
            bOpened = false;
            if (mOnDrawerCloseListener != null) {
                mOnDrawerCloseListener.onDrawerClosed();
            }
        }
        return true;
    }
    private void readyOpen() {
        if(mScrollerCompat != null){
            if(!mScrollerCompat.isFinished()){
                mScrollerCompat.abortAnimation();
            }
        }
        mForceStop = true;
        mScrollerCompat = ScrollerCompat.create(mContext, new Interpolator() {
            public float getInterpolation(float t) {//e^(-4*x)*sin(3.1415926*25/9*x - 11*3.1415926/18) + 1
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        });
        if (DEBUG_DRAWER)
            LogUtil.d("readyOpen -----" + this.getTop() + "-----" + this.getHeight() + "----" + mMaxMove);
        mScrollerCompat.startScroll(0, mHeight, 0,  -mHeight, 200);
        mState = STATE_READY;
        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener.onScrollStarted();
        }
        invalidate();
    }
    public boolean closeHandle() {
        if(mScrollerCompat != null){
            if(!mScrollerCompat.isFinished()){
                mScrollerCompat.abortAnimation();
            }
        }
        mForceStop = true;
        mScrollerCompat = ScrollerCompat.create(mContext, new Interpolator() {
            public float getInterpolation(float t) {//e^(-4*x)*sin(3.1415926*25/9*x - 11*3.1415926/18) + 1
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        });
        if (DEBUG_DRAWER)
            LogUtil.d("closeHandle -----" + this.getTop() + "-----" + this.getHeight() + "----" + mMaxMove);
        mScrollerCompat.startScroll(0, (int)getTranslationY(), 0,  -(int)(getTranslationY() - mHeight), 200);
        mState = STATE_SCROLLING_DOWN;
        invalidate();
        return true;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (DEBUG_DRAWER)
            LogUtil.d("--- start onMeasure --");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        if (DEBUG_DRAWER)
            LogUtil.d("--- start onLayout --");
        super.onLayout(changed, l, t, r, b);
        refreshMyViewHeight();
        if (first && mFollowerHeight > 200) {//mFollowerHeight first time is 75? why?
            mFollowerView.setTranslationY(mFollowerHeight);
            mContentView.setTranslationY(mFollowerHeight);
            first = false;
        }
    }
    private boolean first = true;
    private void refreshMyViewHeight() {
        mFollowerHeight = mFollowerView.getHeight();
        mHeight = this.getHeight() - DensityUtil.dip2px(mContext, 20);
        mContentHeight = mContentView.getHeight();
        mMaxMove = mContentHeight - mHeight;
        if (DEBUG_DRAWER)
            LogUtil.d("first layout follower height:" + mFollowerHeight + " mHeight:" + mHeight + " content height:" + mContentHeight);
    }

    public int getContentHeight() {
        return mContentHeight;
    }

    public void setContentHeight(int mContentHeight) {
        this.mContentHeight = mContentHeight;
    }

    public View getContentView() {
        return mContentView;
    }

    public void setContentView(View mContentView) {
        this.mContentView = mContentView;
    }
    public ImageView getBlurView() {
        return mBlurView;
    }

    public void setBlurView(ImageView img_blur) {
        this.mBlurView = img_blur;
    }

    public int getFollowerHeight() {
        return mFollowerHeight;
    }

    public void setFollowerHeight(int mFollowHeight) {
        this.mFollowerHeight = mFollowHeight;
    }

    public int getMaxMove() {
        return mMaxMove;
    }

    public void setMaxMove(int mMainHeight) {
        this.mMaxMove = mMainHeight;
    }
    public View getFollowerView() {
        return mFollowerView;
    }

    public void setFollowerView(View mFollowContent) {
        this.mFollowerView = mFollowContent;
    }

    public OnDrawerOpenListener getOnDrawerOpenListener() {
        return mOnDrawerOpenListener;
    }

    public void setOnDrawerOpenListener(OnDrawerOpenListener mOnDrawerOpenListener) {
        this.mOnDrawerOpenListener = mOnDrawerOpenListener;
    }

    public OnDrawerCloseListener getOnDrawerCloseListener() {
        return mOnDrawerCloseListener;
    }

    public void setOnDrawerCloseListener(OnDrawerCloseListener mOnDrawerCloseListener) {
        this.mOnDrawerCloseListener = mOnDrawerCloseListener;
    }

    public OnDrawerScrollListener getOnDrawerScrollListener() {
        return mOnDrawerScrollListener;
    }

    public void setOnDrawerScrollListener(OnDrawerScrollListener mOnDrawerScrollListener) {
        this.mOnDrawerScrollListener = mOnDrawerScrollListener;
    }
}
