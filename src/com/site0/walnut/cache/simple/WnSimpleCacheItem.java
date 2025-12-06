package com.site0.walnut.cache.simple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.util.Ws;

public class WnSimpleCacheItem<T> {

    private String key;
    private T data;
    private int weight = 0;
    private long expireAt;
    private long duInMs;

    private WnSimpleCacheItem<T> prev;
    private WnSimpleCacheItem<T> next;

    public WnSimpleCacheItem(String key, T data, long duInMs) {
        this.key = key;
        this.data = data;
        this.weight = 0;
        this.duInMs = duInMs;
        this.expireAt = System.currentTimeMillis() + duInMs;
        this.prev = null;
        this.next = null;
    }

    public String toString() {
        return String.format("[%s](%d):%s", key, weight, data);
    }

    public String toDumpInfo() {
        return String.format("Cache[%s]:%s\n:prev:%s\n:next:%s",
                             key,
                             data,
                             this.toPrevChainAsKeyStr(),
                             this.toNextChainAsKeyStr());
    }

    public List<WnSimpleCacheItem<T>> toPrevChain() {
        List<WnSimpleCacheItem<T>> list = new LinkedList<>();
        WnSimpleCacheItem<T> li = this;
        while (null != li) {
            list.add(li);
            li = li.prev;
        }
        return list;
    }

    public String toPrevChainAsKeyStr() {
        List<WnSimpleCacheItem<T>> list = this.toPrevChain();
        List<String> keys = new ArrayList<>(list.size());
        for (WnSimpleCacheItem<T> li : list) {
            keys.add(li.getKey());
        }
        return Ws.join(keys, "<");
    }

    public List<WnSimpleCacheItem<T>> toNextChain() {
        List<WnSimpleCacheItem<T>> list = new LinkedList<>();
        WnSimpleCacheItem<T> li = this;
        while (null != li) {
            list.add(li);
            li = li.next;
        }
        return list;
    }

    public String toNextChainAsKeyStr() {
        List<WnSimpleCacheItem<T>> list = this.toNextChain();
        List<String> keys = new ArrayList<>(list.size());
        for (WnSimpleCacheItem<T> li : list) {
            keys.add(li.getKey());
        }
        return Ws.join(keys, ">");
    }

    /**
     * 检查缓存项是否已过期
     * 
     * @return true 如果当前时间超过过期时间，否则 false
     */
    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return now > this.expireAt;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public long getDuration() {
        return duInMs;
    }

    public void setDuInMs(long duInMs) {
        this.duInMs = duInMs;
    }

    /**
     * 更新缓存项的过期时间（通常发生在访问时）
     * 
     * @return 更新后的过期时间戳（毫秒）
     */
    public long touch() {
        this.expireAt = System.currentTimeMillis() + duInMs;
        return this.expireAt;
    }

    /**
     * 判断两个缓存项是否代表相同条目（通常通过键比较）
     * 
     * @param item
     *            待比较的缓存项
     * @return true 如果代表相同条目，否则 false
     */
    public boolean isSame(WnSimpleCacheItem<T> item) {
        if (null == item) {
            return false;
        }
        return this.key.equals(item.getKey());
    }

    /**
     * 获取缓存项的键
     * 
     * @return 缓存键
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取缓存项权重（用于淘汰策略）
     * 
     * @return 权重值，权重越高越不易被淘汰
     */
    public int getWeight() {
        return weight;
    }

    /**
     * 增加缓存项权重（通常发生在访问时）
     * 
     * @return 增加后的权重值
     */
    public int increaseWeight() {
        if (Integer.MAX_VALUE == this.weight) {
            return this.weight;
        }
        return ++weight;
    }

    /**
     * 获取缓存项存储的数据
     * 
     * @return 缓存数据对象
     */
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 检查是否存在前驱节点
     * 
     * @return true 如果链表中有前驱节点
     */
    public boolean hasPrev() {
        return null != prev;
    }

    /**
     * 检查是否存在后继节点
     * 
     * @return true 如果链表中有后继节点
     */
    public boolean hasNext() {
        return null != next;
    }

    /**
     * 获取链表中的前驱节点
     * 
     * @return 前驱节点，若不存在返回 null
     */
    public WnSimpleCacheItem<T> prev() {
        return prev;
    }

    /**
     * 获取链表中的后继节点
     * 
     * @return 后继节点，若不存在返回 null
     */
    public WnSimpleCacheItem<T> next() {
        return next;
    }

    /**
     * 设置前驱节点（不维护链表完整性）
     * 
     * @param prev
     *            要设置的前驱节点
     */
    public void setPrev(WnSimpleCacheItem<T> prev) {
        this.prev = prev;
    }

    /**
     * 设置后继节点（不维护链表完整性）
     * 
     * @param next
     *            要设置的后继节点
     */
    public void setNext(WnSimpleCacheItem<T> next) {
        this.next = next;
    }

    /**
     * 在当前节点前插入新节点（维护双向链表关系）
     * 
     * <pre>
     * 操作说明： 
     * 1. 将新节点的 next 指向当前节点 
     * 2. 将新节点的 prev 指向当前节点的原前驱 
     * 3. 如果当前节点有原前驱，则将其 next 指向新节点 
     * 4. 将当前节点的 prev 指向新节点
     * </pre>
     * 
     * 注意：不维护新节点原有的链表关系，调用方需确保新节点不在链表中
     * 
     * @param prev
     *            要插入的前驱节点
     */
    public void insertAsMyPrev(WnSimpleCacheItem<T> prev) {
        if (prev == null) {
            this.prev = prev;
            return;
        }

        // 我的旧前驱节点
        WnSimpleCacheItem<T> oldPrev = this.prev;

        // 建立新关系
        prev.setNext(this);
        prev.setPrev(oldPrev);
        if (null != oldPrev) {
            oldPrev.setNext(prev);
        }
        this.setPrev(prev);
    }

    /**
     * 在当前节点后插入新节点（维护双向链表关系）
     * 
     * <pre>
     * 操作说明： 
     * 1. 将新节点的 prev 指向当前节点 
     * 2. 将新节点的 next 指向当前节点的原后继 
     * 3. 如果当前节点有原后继，则将其 prev 指向新节点 
     * 4. 将当前节点的 next 指向新节点
     * </pre>
     * 
     * 注意：不维护新节点原有的链表关系，调用方需确保新节点不在链表中
     * 
     * @param next
     *            要插入的后继节点
     */
    public void insertAsMyNext(WnSimpleCacheItem<T> next) {
        if (next == null) {
            this.next = null;
            return;
        }

        // 我的旧后继节点
        WnSimpleCacheItem<T> oldNext = this.next;

        // 建立新关系
        next.setPrev(this);
        next.setNext(oldNext);
        if (null != oldNext) {
            oldNext.setPrev(next);
        }
        this.setNext(next);

    }

    /**
     * 用新节点替换当前节点（自动维护链表关系）
     * 
     * @param newItem
     *            用于替换的新节点，null 表示移除当前节点
     */
    public void replaceBy(WnSimpleCacheItem<T> newItem) {
        // null 则表示将自己从链表中移除
        if (null == newItem) {
            this.remove();
        }
        // 将新项目替换到链表中
        else {
            newItem.setPrev(this.prev);
            newItem.setNext(this.next);
            if (null != this.prev) {
                this.prev.setNext(newItem);
            }
            if (null != this.next) {
                this.next.setPrev(newItem);
            }
        }

    }

    /**
     * 将当前节点从链表中移除（自动连接前后节点）
     */
    public void remove() {
        if (null != this.prev) {
            this.prev.setNext(this.next);
        }
        if (null != this.next) {
            this.next.setPrev(this.prev);
        }

    }

    /**
     * 将当前节点移动到目标节点的前驱位置 （先移除自身，再插入到目标节点前）
     * 
     * @param target
     *            目标节点
     */
    public void moveToPrev(WnSimpleCacheItem<T> target) {
        // 删掉自己的位置
        this.remove();

        // 将自己设置到指定位置
        if (null != target) {
            target.insertAsMyPrev(this);
        }

    }

    /**
     * 将当前节点移动到目标节点的后继位置 （先移除自身，再插入到目标节点后）
     * 
     * @param target
     *            目标节点
     */
    public void moveToNext(WnSimpleCacheItem<T> target) {
        // 删掉自己的位置
        this.remove();

        // 将自己设置到指定位置
        if (null != target) {
            target.insertAsMyNext(this);
        }

    }

}
