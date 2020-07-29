package org.nutz.walnut.api.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;

public interface WnIo extends WnStore, WnTree {

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

    String readText(WnObj o);

    BufferedImage readImage(WnObj o);

    long readAndClose(WnObj o, OutputStream ops);

    <T> T readJson(WnObj o, Class<T> classOfT);

    long writeImage(WnObj o, RenderedImage im);

    long writeText(WnObj o, CharSequence cs);

    long appendText(WnObj o, CharSequence cs);

    long writeJson(WnObj o, Object obj, JsonFormat fmt);

    long writeAndClose(WnObj o, InputStream ins);

    Reader getReader(WnObj o, long off);

    Writer getWriter(WnObj o, long off);

    WnIoHandle openHandle(WnObj o, int mode) throws WnIoHandleMutexException;

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
