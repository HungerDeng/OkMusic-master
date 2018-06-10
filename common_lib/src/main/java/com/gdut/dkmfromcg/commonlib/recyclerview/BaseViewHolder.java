package com.gdut.dkmfromcg.commonlib.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by dkmFromCG on 2018/3/10.
 * function:
 */

public class BaseViewHolder extends RecyclerView.ViewHolder {
    /**
     * Views indexed with their IDs
     */
    private final SparseArray<View> mViews;
    public BaseViewHolder(View view) {
        super(view);
        this.mViews = new SparseArray<>();
    }
    @SuppressWarnings("unchecked")
    public  <T extends View> T findViewById(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }
}
