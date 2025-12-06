package com.site0.walnut.cache.simple;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.cache.WnCache;
import com.site0.walnut.util.Ws;

/**
 * 一个线程安全的缓存类
 * <p>
 * 这个类实际除了维护一个 Map 用来快速检索缓存项以外，还通过一个链表来维护缓存项的热度
 * <ul>
 * <li>每次命中缓存，缓存项权重 +1，一直到最大值
 * </ul>
 * 
 * <pre>
 * [ Hot  ]    <- head 权重最大
 *  ...
 * [ Cool ]    <- tail 权重最小
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSimpleCache<T> implements WnCache<T> {

    private Map<String, WnSimpleCacheItem<T>> _cache;

    private int minItemCount;

    private int maxItemCount;

    private long duInMs;

    WnSimpleCacheItem<T> head;

    WnSimpleCacheItem<T> tail;

    public WnSimpleCache() {
        this(100, 200, 30);
    }

    public WnSimpleCache(int minItemCount, int maxItemCount, int duInSec) {
        this._cache = new HashMap<>();
        this.minItemCount = minItemCount;
        this.maxItemCount = maxItemCount;
        this.duInMs = duInSec * 1000L;
    }

    @Override
    public String toString() {
        String str = String.format("SimpleCache size=%d (%d-%d) itemDu=%dms\n",
                                   _cache.size(),
                                   minItemCount,
                                   maxItemCount,
                                   duInMs);
        StringBuilder sb = new StringBuilder(str);
        sb.append(String.format(" - head: %s\n", head));
        if (null != head) {
            int n = 10;
            WnSimpleCacheItem<T> current = this.head.next();
            for (int i = 0; i < n; i++) {
                if (null == current)
                    break;
                String s = String.format("  - [%d]: %s\n", i + 1, current);
                sb.append(s);
                current = current.next();
            }
            if (null != current) {
                sb.append("...\n");
            }
        }
        sb.append(String.format(" - tail: %s\n", tail));
        return sb.toString();
    }

    private void __put_item(WnSimpleCacheItem<T> item) {
        // 如果已存在则先移除
        if (_cache.containsKey(item.getKey())) {
            __remove_item(_cache.get(item.getKey()));
        }

        _cache.put(item.getKey(), item);

        if (head == null) {
            head = tail = item;
            return;
        }

        // 寻找插入位置: 第一个高于 item 权重的节点
        WnSimpleCacheItem<T> current = tail;
        int itw = item.getWeight();
        while (current != null && current.getWeight() <= itw) {
            current = current.prev();
        }

        // 找不到，那么我就是最高权重节点，应该作为 head
        if (current == null) {
            // 头尾相同的单节点
            if (head == tail) {
                tail.insertAsMyPrev(item);
                head = item;
            }
            // 作为节点头
            else {
                head.insertAsMyPrev(item);
                head = item;
            }
        }
        // 都比我权重高，那么我应该作为 tail
        else if (current == tail) {
            // 插入到尾部
            tail.insertAsMyNext(item);
            tail = item;
        }
        // 只有头节点比我权限高, 那么我就在它后面好了
        else if (current == head) {
            head.insertAsMyNext(item);
        }
        // 那么就插入到给我的节点后面
        else {
            current.insertAsMyNext(item);
        }

    }

    private void __remove_item(WnSimpleCacheItem<T> item) {
        if (item == null)
            return;

        // 更新头尾指针
        if (head == item)
            head = item.next();
        if (tail == item)
            tail = item.prev();

        // 从链表中移除
        item.remove();
        _cache.remove(item.getKey());
    }

    private void __clear_items() {
        // 清理过期项
        WnSimpleCacheItem<T> current = this.tail;
        while (current != null) {
            WnSimpleCacheItem<T> prev = current.prev();
            if (current.isExpired()) {
                __remove_item(current);
            }
            current = prev;
        }

        // 清理多余项
        if (_cache.size() >= maxItemCount) {
            while (_cache.size() > minItemCount && tail != null) {
                WnSimpleCacheItem<T> toRemove = tail;
                tail = toRemove.prev();
                __remove_item(toRemove);
            }
        }

    }

    private void __clear_all() {
        this.head = null;
        this.tail = null;
        this._cache.clear();
    }

    @Override
    public synchronized T get(String key) {
        WnSimpleCacheItem<T> item = _cache.get(key);
        if (null == item || item.isExpired()) {
            T itemData = loadItemData(key);
            if (null == itemData) {
                return null;
            }
            item = this.createItem(key, itemData);
            item.increaseWeight();
            __put_item(item);
        }
        // 增加一下自身的权重
        else {
            int myWeight = item.increaseWeight();
            item.touch();
            // 到头了，就不用移动了
            if (this.head != item) {
                WnSimpleCacheItem<T> current = item.prev();
                while (null != current && current.getWeight() <= myWeight) {
                    current = current.prev();
                }
                if (this.tail == item) {
                    this.tail = item.prev();
                }
                if (null == current) {
                    item.moveToPrev(this.head);
                    this.head = item;
                } else {
                    item.moveToNext(current);
                }
            }

        }

        // 返回结果
        return item.getData();
    }

    @Override
    public synchronized void put(String key, T data) {
        __put_and_return(key, data);
    }

    private WnSimpleCacheItem<T> __put_and_return(String key, T data) {
        // 防空
        if (Ws.isBlank(key) || null == data) {
            return null;
        }
        // 已经存在
        WnSimpleCacheItem<T> item = this._cache.get(key);
        if (null != item) {
            item.touch();
            item.increaseWeight();
            item.setData(data);
        }
        // 创建一个新项目
        else {
            if (size() >= this.maxItemCount) {
                this.__clear_items();
            }
            item = this.createItem(key, data);
            __put_item(item);
        }
        return item;
    }

    @Override
    public synchronized T remove(String key) {
        WnSimpleCacheItem<T> item = _cache.get(key);
        __remove_item(item);
        return null == item ? null : item.getData();
    }

    @Override
    public synchronized void cleanUp() {
        this.__clear_items();
    }

    @Override
    public synchronized void clearAll() {
        this.__clear_all();
    }

    public WnSimpleCacheItem<T> createItem(String key, T data) {
        return new WnSimpleCacheItem<>(key, data, duInMs);
    }

    /**
     * 子类可以重载这个方法，用来提供懒加载的功能
     * 
     * @param key
     *            对象键
     * @return 加载后的数据
     */
    protected T loadItemData(String key) {
        return null;
    }

    @Override
    synchronized public int size() {
        return this._cache.size();
    }

    
    public int getMinItemCount() {
        return minItemCount;
    }

    public void setMinItemCount(int minItemCount) {
        this.minItemCount = minItemCount;
    }

    public int getMaxItemCount() {
        return maxItemCount;
    }

    public void setMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
    }

    public long getDuInMs() {
        return duInMs;
    }

    public void setDuInMs(long duInMs) {
        this.duInMs = duInMs;
    }
    
    

}
