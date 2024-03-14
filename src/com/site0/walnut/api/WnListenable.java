package com.site0.walnut.api;

import java.util.List;

public interface WnListenable<T> {

    /**
     * 为某事件添加一个监听器
     * 
     * @param eventName
     *            事件名称
     * @param li
     *            监听器
     */
    void addEventListener(String eventName, WnEventListener<T> li);

    /**
     * 移除一个或者多个监听器
     * 
     * @param eventName
     *            事件名称，如果传入 null 则表示清除所有事件的监听器
     * @param li
     *            要移除的监听器, null 表示清除该事件下全部监听器
     * 
     * @return 被删除的监听器列表，一定不会为 null
     */
    List<WnEventListener<T>> removeEventListener(String eventName, WnEventListener<T> li);

    /**
     * @param eventName
     *            事件名
     * @return 事件下全部监听器，如果事件名为null，则返回空列表
     */
    List<WnEventListener<T>> getEventListener(String eventName);

    /**
     * 触发一个事件
     * 
     * @param eventName
     *            事件名
     * @param obj
     *            事件参数
     */
    void fireEvent(String eventName, T obj);

}
