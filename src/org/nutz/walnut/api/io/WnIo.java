package org.nutz.walnut.api.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.agg.WnAggOptions;
import org.nutz.walnut.api.io.agg.WnAggResult;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.core.WnIoMappingFactory;

public interface WnIo {

    WnIoMappingFactory getMappingFactory();

    boolean exists(WnObj p, String path);

    boolean existsId(String id);

    WnObj checkById(String id);

    WnObj check(WnObj p, String path);

    WnObj fetch(WnObj p, String path);

    WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex);

    void walk(WnObj p, Callback<WnObj> callback, WalkMode mode);

    void walk(WnObj p, Callback<WnObj> callback, WalkMode mode, WnObjFilter filter);

    WnObj move(WnObj src, String destPath);

    WnObj move(WnObj src, String destPath, int mode);

    WnObj rename(WnObj o, String nm);

    WnObj rename(WnObj o, String nm, boolean keepType);

    WnObj rename(WnObj o, String nm, int mode);

    void set(WnObj o, String regex);

    /**
     * 设置某对象的一个值，并直接返回设置前/后的对象元数据
     * 
     * @param id
     *            对象 ID
     * @param map
     *            要修改的值表
     * @param returnNew
     *            如果是 true 返回修改后的值
     * @return 修改前/后 对象
     */
    WnObj setBy(String id, NutBean map, boolean returnNew);

    /**
     * 设置符合条件的某一对象的一组值，并直接返回设置前/后的对象元数据
     * 
     * @param q
     *            对象查询条件
     * @param map
     *            要修改的值表
     * @param returnNew
     *            如果是 true 返回修改后的值
     * @return 修改前/后 对象
     */
    WnObj setBy(WnQuery q, NutBean map, boolean returnNew);

    /**
     * 返回修改前/后值
     * 
     * @see #inc(WnQuery, String, int, boolean)
     */
    int inc(String id, String key, int val, boolean returnNew);

    /**
     * 「同步」修改符合条件的某对象的某个整型元数据，并返回
     * 
     * @param q
     *            对象查询条件
     * @param key
     *            元数据名称
     * @param val
     *            修改的值
     * @param returnNew
     *            如果是 true 返回修改后的值
     * @return 修改前/后的值
     */
    int inc(WnQuery q, String key, int val, boolean returnNew);

    int getInt(String id, String key, int dft);

    long getLong(String id, String key, long dft);

    String getString(String id, String key, String dft);

    <T> T getAs(String id, String key, Class<T> classOfT, T dft);

    WnObj get(String id);

    WnObj getIn(WnObj p, String id);

    WnObj getOne(WnQuery q);

    WnObj getRoot();

    String getRootId();

    boolean isRoot(String id);

    boolean isRoot(WnObj o);

    int each(WnQuery q, Each<WnObj> callback);

    List<WnObj> query(WnQuery q);

    int eachChild(WnObj o, String name, Each<WnObj> callback);

    List<WnObj> getChildren(WnObj o, String name);

    long count(WnQuery q);

    /**
     * 根据一个指定条件，对一个键进行聚集汇总计算
     * 
     * @param q
     *            过滤条件，限制，以及排序方式
     * @param agg
     *            聚集的方式
     * @return 聚集结果
     */
    WnAggResult aggregate(WnQuery q, WnAggOptions agg);

    boolean hasChild(WnObj p);

    // WnObj getDirect(String id);

    WnObj push(String id, String key, Object val, boolean returnNew);

    void push(WnQuery query, String key, Object val);

    WnObj pull(String id, String key, Object val, boolean returnNew);

    void pull(WnQuery query, String key, Object val);

    WnObj create(WnObj p, WnObj o);
    
    WnObj createIfNoExists(WnObj p, WnObj o);
    
    WnObj createIfExists(WnObj p, WnObj o);

    WnObj create(WnObj p, String path, WnRace race);

    WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race);

    WnObj createById(WnObj p, String id, String name, WnRace race);

    WnObj createIfNoExists(WnObj p, String path, WnRace race);

    WnObj createIfExists(WnObj p, String path, WnRace race);

    /**
     * 返回修改前对象
     * 
     * @see org.nutz.walnut.api.io.WnTree#setBy(String, Map)
     */
    WnObj setBy(String id, String key, Object val, boolean returnNew);

    /**
     * @see org.nutz.walnut.api.io.WnTree#setBy(String, NutMap, boolean)
     */
    WnObj setBy(WnQuery q, String key, Object val, boolean returnNew);

    void setMount(WnObj o, String mnt);

    void writeMeta(WnObj o, Object meta);

    void appendMeta(WnObj o, Object meta);

    void appendMeta(WnObj o, Object meta, boolean keepType);

    String readText(WnObj o);

    byte[] readBytes(WnObj o);

    BufferedImage readImage(WnObj o);

    long readAndClose(WnObj o, OutputStream ops);

    <T> T readJson(WnObj o, Class<T> classOfT);

    long writeImage(WnObj o, RenderedImage im);

    long writeText(WnObj o, CharSequence cs);

    long appendText(WnObj o, CharSequence cs);

    long writeJson(WnObj o, Object obj, JsonFormat fmt);

    long writeBytes(WnObj o, byte[] buf);

    long writeBytes(WnObj o, byte[] buf, int off, int len);

    long writeAndClose(WnObj o, InputStream ins);

    Reader getReader(WnObj o, long off);

    Writer getWriter(WnObj o, long off);

    WnIoIndexer getIndexer(WnObj o);

    WnIoHandle openHandle(WnObj o, int mode) throws WnIoHandleMutexException, IOException;

    /**
     * 给出一个快捷的方法，将对象 A 的内容快速 copy 到对象B 中
     * 
     * @param a
     *            对象A
     * @param b
     *            对象B
     * @return 0 表示失败，非零的数，不同的实现类可以有不同的定义
     */
    long copyData(WnObj a, WnObj b);

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

    void delete(WnObj o, boolean r);

    /**
     * 将对象剪裁到给定大小
     * 
     * @param o
     *            对象
     * @param len
     *            大小
     */
    void trancate(WnObj o, long len);

    /**
     * 获取当前句柄偏移量
     * 
     * @param hid
     * @return
     */
    long getPos(String hid);

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

    MimeMap mimes();

}
