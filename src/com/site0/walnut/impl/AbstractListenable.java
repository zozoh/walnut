package com.site0.walnut.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.site0.walnut.api.WnEventListener;
import com.site0.walnut.api.WnListenable;

public abstract class AbstractListenable<T> implements WnListenable<T> {

    private Map<String, List<WnEventListener<T>>> listeners;

    public AbstractListenable() {
        this.listeners = new HashMap<>();
    }

    @Override
    public List<WnEventListener<T>> getEventListener(String eventName) {
        List<WnEventListener<T>> reList = new LinkedList<>();
        if (null != eventName) {
            List<WnEventListener<T>> list = listeners.remove(eventName);
            if (null != list) {
                reList.addAll(list);
            }
        }
        return reList;
    }

    @Override
    public void fireEvent(String eventName, T obj) {
        List<WnEventListener<T>> list = this.getEventListener(eventName);
        for (WnEventListener<T> li : list) {
            li.invoke(eventName, obj);
        }
    }

    public void addEventListener(String eventName, WnEventListener<T> li) {
        if (null == li) {
            return;
        }
        List<WnEventListener<T>> list = listeners.get(eventName);
        if (list == null) {
            list = new LinkedList<>();
            listeners.put(eventName, list);
        }
        list.add(li);
    }

    public List<WnEventListener<T>> removeEventListener(String eventName, WnEventListener<T> li) {
        List<WnEventListener<T>> reList = new LinkedList<>();
        // 移除全部
        if (null == eventName) {
            for (List<WnEventListener<T>> list : listeners.values()) {
                if (null != list) {
                    reList.addAll(list);
                }
            }
            this.listeners = new HashMap<>();
        }
        // 移除指定事件全部
        else if (null == li) {
            List<WnEventListener<T>> list = listeners.remove(eventName);
            if (null != list) {
                reList.addAll(list);
            }
        }
        // 移除指定监听器
        else {
            List<WnEventListener<T>> list = listeners.remove(eventName);
            if (null != list) {
                if (list.remove(li))
                    reList.add(li);
            }
        }
        // 搞定
        return reList;
    }

}
