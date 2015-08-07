package org.nutz.walnut.api.io;

import org.nutz.walnut.util.UnitTestable;

public interface WnStore extends UnitTestable {

    /**
     * 打开一个句柄
     * 
     * @param o
     *            对象
     * @param mode
     *            打开模式
     * @return hid 句柄ID
     */
    String open(WnObj o, int mode);

    /**
     * 将缓冲中的内容写入到对应的桶内
     * 
     * @param hid
     *            句柄ID
     * 
     * @return 对象。与 close() 方法的返回做相同的处理
     * 
     * @see #close(String)
     */
    WnObj flush(String hid);

    /**
     * 关闭一个句柄
     * 
     * @param hid
     *            句柄ID
     * @return 对象，其中写入模式的句柄会修改 sha1,len,lm,data 字段。<br>
     *         并且给对象增加一个元数据 "__store_update_meta"
     * 
     * @see org.nutz.walnut.api.io.WnObj#hasRWMetaKeys()
     * @see org.nutz.walnut.api.io.WnObj#getRWMetaKeys()
     * @see org.nutz.walnut.api.io.WnObj#setRWMetaKeys(String)
     */
    WnObj close(String hid);

    /**
     * 从存储中读取字节
     * 
     * @param hid
     *            句柄ID
     * @param bs
     *            缓冲
     * @param off
     *            从什么位置开始写缓冲
     * @param len
     *            最多写多少字节
     * @return 实际向缓冲写了多少字节
     */
    int read(String hid, byte[] bs, int off, int len);

    /**
     * 向存储中写入字节
     * 
     * @param hid
     *            句柄ID
     * @param bs
     *            缓冲
     * @param off
     *            从缓冲什么位置开始读取字节
     * @param len
     *            读取多少字节
     */
    void write(String hid, byte[] bs, int off, int len);

    /**
     * @see #read(byte[], int, int)
     */
    int read(String hid, byte[] bs);

    /**
     * @see #write(byte[], int, int)
     */
    void write(String hid, byte[] bs);

    /**
     * 移动句柄的读写指针的位置。追加模式的句柄不支持此操作
     * 
     * @param hid
     *            句柄 ID
     * @param pos
     *            移动读写指针到新的位置
     * 
     * @throws "e.io.seek.append"
     *             追加模式抛错
     */
    void seek(String hid, long pos);

    /**
     * 删除对象对应的存储空间
     * 
     * @param o
     *            对象
     */
    void delete(WnObj o);

}
