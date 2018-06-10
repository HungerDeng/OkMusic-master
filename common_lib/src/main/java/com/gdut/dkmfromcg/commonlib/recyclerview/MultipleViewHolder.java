package com.gdut.dkmfromcg.commonlib.recyclerview;

import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;

/**
 * Created by dkmFromCG on 2018/3/20.
 * function:
 */

public class MultipleViewHolder extends BaseViewHolder {

    private MultipleViewHolder(View view) {
        super(view);
    }

    public static MultipleViewHolder create(View view) {
        return new MultipleViewHolder(view);
    }
}
