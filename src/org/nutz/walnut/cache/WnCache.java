package org.nutz.walnut.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个线程安全的缓存类
 * <p>
 * 这个类实际除了维护一个 Map 用来快速检索缓存项以外，还通过一个链表来维护缓存项的热度
 * <ul>
 * <li>每次命中缓存，缓存项权重 +1，一直到最大值
 * </ul>
 * 
 * <pre>
 * [ Hot  ]    <- top
 *  ...
 * [ Cool ]    <- bottom
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnCache<T> {

    private Map<String, WnCacheItem<T>> cache;

    private int maxItemCount;

    private int len;

    private WnCacheItem<T> top;

    private WnCacheItem<T> bottom;

    public WnCache() {
        this(1000);
    }

    public WnCache(int maxItemCount) {
        this.cache = new HashMap<>();
        this.maxItemCount = maxItemCount;
    }

    public synchronized T get(String key) {
        WnCacheItem<T> item = cache.get(key);
        if (null == item) {
            return null;
        }
        // 增加一下自身的权重
        int wei = item.increaseWeight();

        // 向上找一个最
        WnCacheItem<T> ta = item.prev();
        if (ta.getWeight() < wei) {
            do {
                ta = ta.prev();
            } while (null != ta && ta.getWeight() < wei);

            // 从原有位置脱离
            if (item.hasPrev()) {
                item.prev().setNext(item.next());
            }
            if (item.hasNext()) {
                item.next().setPrev(item.prev());
            }

            // 是否要改变顶
            if (null == ta) {
                item.setPrev(null);
                item.setNext(this.top);
                this.top = item;
            }
            // 插入到目标后面
            else {
                if (ta.hasNext()) {
                    ta.next().setPrev(item);
                }
                item.setPrev(ta);
                item.setNext(ta.next());
                ta.setNext(item);
            }
        }

        // 返回结果
        return item.getData();
    }

    public synchronized void addItem(T data) {
        WnCacheItem<T> item = this.createItem(data);
        // 空的，那么初始化
        if (null == bottom) {
            this.bottom = item;
            this.top = item;
        }
        // 否则接续到队列末尾
        else {
            this.bottom.setNext(item);
            item.setPrev(this.bottom);
            this.bottom = item;
        }
        this.len++;
    }

    public synchronized T remove(String key) {
        WnCacheItem<T> item = cache.get(key);
        if (null != item) {
            // 对齐顶
            if (this.top == item) {
                this.top = item.next();
            }
            // 对齐底
            if (this.bottom == item) {
                this.bottom = item.prev();
            }
            // 从链表中删掉
            if (item.hasPrev()) {
                item.prev().setNext(item.next());
            }
            if (item.hasNext()) {
                item.next().setPrev(item.prev());
            }

            // 搞定
            this.len--;
            return item.getData();
        }
        return null;
    }

    public int getSize() {
        return this.len;
    }

    protected abstract WnCacheItem<T> createItem(T data);

    protected abstract WnCacheItem<T> loadItem(String key);

    public int getMaxItemCount() {
        return maxItemCount;
    }

    public int getLen() {
        return len;
    }

}
