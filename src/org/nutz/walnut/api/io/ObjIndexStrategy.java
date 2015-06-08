package org.nutz.walnut.api.io;

/**
 * 当生成对象索引的时候，用什么策略获取 cmg (creator,mender, group)
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum ObjIndexStrategy {

    /**
     * 遵循祖先
     */
    PARENT,

    /**
     * 从当前的上下文中获取
     */
    WC,

    /**
     * 严格模式，即不自动生成索引
     */
    STRICT,

    /**
     * 宽松模式，即不会自动生成索引，但是会返回索引对象
     */
    IMPLICT
}
