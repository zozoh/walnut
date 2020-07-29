package org.nutz.walnut.core;

/**
 * 句柄管理
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnIoHandleManager {

    /**
     * 根据传入的句柄对象，分配句柄。
     * <p>
     * 这个函数保证了写模式（包括追加）只能有一个句柄
     * 
     * @param h
     *            待分配的句柄对象
     */
    void alloc(WnIoHandle h);

    /**
     * 取回一个句柄。这个主要是给 IO 层实现类用的
     * 
     * @param hid
     *            句柄 ID
     * @return 一个已经存在的 Handle
     * 
     * @throws "e.io.handle.NoExists":
     *             句柄不存在
     */
    WnIoHandle check(String hid);

    /**
     * 获取一个句柄信息。
     * <p>
     * 这个主要给各个桶管理器使用的
     * 
     * @param hid
     *            句柄 ID
     * @return 句柄信息
     */
    HandleInfo load(String hid);

    /**
     * 根据已经设置了 ID 的句柄，填充句柄其他字段的信息。
     * <p>
     * 这个主要是给各个桶管理器用的
     * 
     * @param h
     *            句柄对象，必须设置了ID
     * @param info
     *            句柄的具体信息，本函数将根据这个对象设置完整的句柄对象
     * 
     * @throws "e.io.handle.NoExists":
     *             句柄不存在
     */
    void setup(WnIoHandle h, HandleInfo info);

    /**
     * 将传入的句柄持久化，这个函数会保证一个对象仅能有一个写句柄。
     * <p>
     * 如果没有给句柄分配ID，本函数应该自动分配一个。
     * 
     * @param h
     *            新创立的句柄对象
     * 
     * @throws "e.io.handle.AlreadyOpened":
     *             已经有一个打开的写句柄
     */
    void save(WnIoHandle h);

    /**
     * @param h
     *            句柄对象
     */
    void touch(WnIoHandle h);

    /**
     * 移除一个句柄对象
     * 
     * @param hid
     *            句柄 ID
     * 
     * @throws "e.io.handle.NoExists":
     *             句柄不存在
     */
    void remove(String hid);
}
