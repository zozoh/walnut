package com.site0.walnut.api;

public interface WnEventListener<T> {

    /**
     * 监听器回调
     * 
     * @param eventName
     *            事件名称
     * 
     * @param obj
     *            输入参数
     */
    void invoke(String eventName, T obj);

}
