package org.nutz.walnut.api.box;

public interface WnBoxService {

    WnBox get(String boxId);

    /**
     * 分配一个空闲的沙箱，如果没有空闲的，则等待到一个 timeout。
     * 
     * @param timeout
     *            等待时间(ms)。 小于0表示永久等待，0 表示不等待
     * @return 一个沙箱实例
     */
    WnBox alloc(long timeout);

    /**
     * 回收一个沙箱。如果这个沙箱还有任务运行，则强制终止
     * 
     * @param box
     *            沙箱实例
     */
    void free(WnBox box);

    /**
     * 关闭所有正在运行的沙箱
     */
    void shutdown();
}
