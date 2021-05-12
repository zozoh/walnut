package org.nutz.walnut.util.stream;

import java.io.InputStream;

public class WnInputStreamInfo {

    /**
     * 流的内容
     */
    public InputStream stream;

    /**
     * 这个流对象对应的名称
     */
    public String name;

    /**
     * 内容类型
     */
    public String mime;

    /**
     * 内容长度
     */
    public long length;

}
