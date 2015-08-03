package org.nutz.walnut.api.io;

/**
 * 根据对象的类型等元数据，来决定在句柄关闭时，对象的桶是否需要封盖
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnBucketSealStrategy {

    boolean shouldSealed(WnObj o);

}
