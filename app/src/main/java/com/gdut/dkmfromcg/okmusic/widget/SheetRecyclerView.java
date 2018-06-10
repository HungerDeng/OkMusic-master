package com.gdut.dkmfromcg.okmusic.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by dkmFromCG on 2018/3/9.
 * function:
 */


public class SheetRecyclerView extends RecyclerView {

    public SheetRecyclerView(Context context) {
        super(context);
    }

    public SheetRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SheetRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, mExpandSpec);
    }
}
