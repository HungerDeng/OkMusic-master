package com.gdut.dkmfromcg.commonlib.recyclerview;


import com.google.auto.value.AutoValue;

/**
 * Created by dkmFromCG on 2018/3/20.
 * function:
 */
@AutoValue
public abstract class RgbValue {

    public abstract int red();

    public abstract int green();

    public abstract int blue();

    public static RgbValue create(int red, int green, int blue) {
        return new AutoValue_RgbValue(red, green, blue);
    }

}
