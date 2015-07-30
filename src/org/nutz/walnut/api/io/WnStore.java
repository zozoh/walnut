package org.nutz.walnut.api.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.walnut.util.UnitTestable;

public interface WnStore extends UnitTestable{

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
     * 关闭一个句柄
     * 
     * @param hid
     *            句柄ID
     */
    void close(String hid);

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
    
    void seek(String hid, long pos);
    
    /**
     * 将缓冲中的内容写入到对应的桶内
     * 
     * @param hid 句柄ID
     */
    void flush(String hid);

    /**
     * 删除对象对应的存储空间
     * 
     * @param o
     *            对象
     */
    void delete(WnObj o);

    /**
     * 获取一个对象的输入流
     * 
     * @param o
     *            对象
     * @param off
     *            0 表示从头读取，-1 非法，>0 表示从某一个特殊位置
     * @return 对象的输入流
     */
    InputStream getInputStream(WnObj o, long off);

    /**
     * 获取一个对象的写入流。当输入流关闭后，会自动更新传入的对象的内部状态。 <br>
     * 更新的字段包括
     * <ul>
     * <li>lm : 最后修改时间
     * <li>len : 内容长度
     * <li>sha1 : 文件的 SHA1 指纹
     * <li>data : 文件的数据键
     * </ul>
     * 
     * @param o
     *            对象
     * @param off
     *            -1 表示从尾部写，0 表示从头覆盖
     * @return 对象的写入流
     */
    OutputStream getOutputStream(WnObj o, long off);

}
