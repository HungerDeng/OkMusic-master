package com.gdut.dkmfromcg.commonlib.recyclerview.data;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by dkmFromCG on 2018/3/19.
 * function: RecyclerViewItem 对应的 ItemEntity,存储JSON的 key和 value
 */

public class MultipleItemEntity implements MultiItemEntity {

    //当 MULTIPLE_FIELDS 实例被回收时,它的弱引用 FIELDS_REFERENCE 会被添加进 ITEM_QUEUE,可轮询 ITEM_QUEUE 来gc掉弱引用
    private final ReferenceQueue<LinkedHashMap<Object, Object>> ITEM_QUEUE = new ReferenceQueue<>();
    //存储 Entity的 key和 value
    private final LinkedHashMap<Object, Object> MULTIPLE_FIELDS = new LinkedHashMap<>();
    //MULTIPLE_FIELDS 的弱引用
    private final SoftReference<LinkedHashMap<Object, Object>> FIELDS_REFERENCE =
            new SoftReference<>(MULTIPLE_FIELDS, ITEM_QUEUE);

    MultipleItemEntity(LinkedHashMap<Object, Object> fields) {
        //FIELDS_REFERENCE.get()不会等于null,是因为 MULTIPLE_FIELDS的强引用一直没被gc掉 ??是吗,不确定
        //FIELDS_REFERENCE.get().putAll(fields); 采用迭代的方式去添加,而不是 for each
        Iterator iterator=fields.entrySet().iterator();
        while (iterator.hasNext()){
            final LinkedHashMap.Entry entry= (LinkedHashMap.Entry) iterator.next();
            FIELDS_REFERENCE.get().put(entry.getKey(),entry.getValue());
        }

    }

    public static MultipleItemEntityBuilder builder(){
        return new MultipleItemEntityBuilder();
    }

    @Override
    public int getItemType() {
        return (int) FIELDS_REFERENCE.get().get(MultipleFields.ITEM_TYPE);
    }

    @SuppressWarnings("unchecked")
    public final <T> T getField(Object key){
        return (T) FIELDS_REFERENCE.get().get(key);
    }

    public final LinkedHashMap<?,?> getFields(){
        return FIELDS_REFERENCE.get();
    }

    //存放键值对
    public final MultipleItemEntity setField(Object key, Object value){
        FIELDS_REFERENCE.get().put(key,value);
        return this;
    }
}


