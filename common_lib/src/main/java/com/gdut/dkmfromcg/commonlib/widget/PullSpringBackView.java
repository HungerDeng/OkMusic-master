package com.gdut.dkmfromcg.commonlib.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.gdut.dkmfromcg.commonlib.util.log.Logger;

/**
 * Created by dkmFromCG on 2018/4/11.
 * function:
 */

public class PullSpringBackView extends FrameLayout {

    private ViewDragHelper mViewDragHelper = null;
    private View mChildView;

    //初识位置
    private int mStartTop;
    private int mStartBottom;

    private Dialog mDialog=null;
    public void bindToDialog(Dialog dialog){
        this.mDialog=dialog;
    }


    public PullSpringBackView(Context context) {
        this(context, null);
    }

    public PullSpringBackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullSpringBackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new RuntimeException("PullSpringBackView 只能有一个子 View ！");
        }
        if (mViewDragHelper == null) {
            mChildView = getChildAt(0);
            mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelperCallBack());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private class ViewDragHelperCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mChildView == child;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            mStartTop = capturedChild.getTop();
            mStartBottom = capturedChild.getBottom();
        }

        //返回拖拽子View在水平方向上可以被拖动的最远距离,处理点击和滑动冲突必须重写该方法
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 0;
        }

        //返回拖拽子View在垂直方向上可以被拖动的最远距离,处理点击和滑动冲突必须重写该方法
        @Override
        public int getViewVerticalDragRange(View child) {
            return mStartBottom;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }

        //竖向可滑动范围
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.min(Math.max(top, mStartTop), mStartBottom);
        }

        //在 onViewReleased() 方法中调用 settleCapturedViewAt() 方法来重定位 child。
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //手指释放时,滑动距离大于一半直接滚动到底部，否则返回顶部
            if (releasedChild == mChildView) {
                float movePercentage = (float) (releasedChild.getTop() - mStartTop) / (float) (mStartBottom - mStartTop);
                Logger.d("mStartTop: ", mStartTop);
                Logger.d("mStartBottom: ",mStartBottom);
                Logger.d("mChildHeight: ",mStartBottom-mStartTop);
                Logger.d("releasedChild.getTop: ", releasedChild.getTop());
                Logger.d("movePercentage: ", movePercentage);
                int finalTop = (movePercentage >= .5f) ? mStartBottom  : mStartTop;
                mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), finalTop);
                invalidate();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (top == mStartBottom){
                if (mDialog!=null){
                    mDialog.dismiss();
                }
            }
        }
    }

}
