package com.gdut.dkmfromcg.commonlib.recyclerview.data;

/**
 * Created by dkmFromCG on 2018/3/19.
 * function: 首页的RecyclerViewItem类型,根据存储JSON数据的类型
 */

public class ItemType {
    public static final int TEXT = 1; //只有 Text
    public static final int IMAGE = 2;//只有 Image
    public static final int TEXT_IMAGE = 3;//有Text 和 Image
    public static final int BANNER = 4;    //轮播图广告
    public static final int VERTICAL_MENU_LIST = 5; //垂直菜单
    public static final int SINGLE_BIG_IMAGE = 6; //单一大图
}
