package org.nutz.walnut.api.io;

import java.io.InputStream;
import java.io.OutputStream;

public interface WnStore extends WnStoreTable {

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

    InputStream getInputStream(WnHistory his, long off);

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
