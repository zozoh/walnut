package org.nutz.walnut.core;

import java.io.IOException;

import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;

public interface WnIoBM {

    /**
     * @param bm
     *            另外一个桶管理器
     * @return 自己与给入的桶管理器是否相同
     */
    boolean isSame(WnIoBM bm);

    /**
     * 打开一个句柄。
     * <p>
     * 这个句柄对象维持着当前读写操作的信息。
     * 
     * @param o
     *            对象
     * @param mode
     *            打开模式
     * @return 句柄对象
     * 
     * @throws WnIoHandleMutexException
     *             创建句柄的条件不具备，譬如支持互斥写的句柄管理器会抛出这个异常
     * @throws IOException
     *             创建句柄时数据读写发生的异常
     */
    WnIoHandle open(WnObj o, int mode, WnIoIndexer indexer)
            throws WnIoHandleMutexException, IOException;

    /**
     * 创建一个属于自己的句柄对象。
     * <p>
     * 因为 WnIoHandleManager 不知道要创建什么对象<br>
     * 它可以通过持久化层得到 <code>HandleInfo</code>， 然后获取<code>WnIoMapping</code>。<br>
     * 这样，只要知道对应的<code>桶管理器</code>需要什么样的句柄，就能取得完整的句柄对象了
     * 
     * @param mode
     *            打开模式
     * 
     * @return 新的空白句柄对象
     */
    WnIoHandle createHandle(int mode);

    /**
     * @param hid
     *            句柄ID
     * @return 对应的句柄对象
     */
    WnIoHandle checkHandle(String hid);

    /**
     * 给出一个快捷的方法，将对象 A 的内容快速 copy 到对象B 中
     * <p>
     * 本函数会直接直接讲目标ID对应的引用计数加一
     * 
     * @param oSr
     *            源对象A
     * @param oTa
     *            目标对象B
     * 
     * @return 当前桶还有多少引用
     */
    long copy(WnObj oSr, WnObj oTa);

    /**
     * 删除对象对应的存储空间
     * <p>
     * 通常，桶管理器应该减去它的引用计数，当归零时，自动删除
     * 
     * @param o
     *            对象
     * 
     * @return 当前目标还有多少引用。 0 表示这个对象被删除了
     */
    long remove(WnObj o);

    /**
     * 将对象剪裁到指定大小
     * 
     * @param o
     *            要剪裁的对象
     * @param len
     *            大小
     * @param indexer
     *            索引管理器，用来修改对象元数据
     * @return 如果为0或者正整数，则为剪裁后的桶大小（字节）否则表示剪裁失败。 <br>
     *         但是实现类不应该返回负数，直接抛出异常会是比较好的做法。<br>
     *         如果实现类真的逆天的返回负数，调用者应该什么也不做，立即返回就是。
     * @throws "e.io.bm.trancate"
     *             : 剪裁异常
     */
    long truncate(WnObj o, long len, WnIoIndexer indexer);

}
