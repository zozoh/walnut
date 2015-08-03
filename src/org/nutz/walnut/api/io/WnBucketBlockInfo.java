package org.nutz.walnut.api.io;

public class WnBucketBlockInfo {

    public WnBucketBlockInfo() {}

    public WnBucketBlockInfo set(int pl, int sz, int pr) {
        this.size = sz;
        this.paddingLeft = pl;
        this.paddingRight = pr;
        return this;
    }

    /**
     * 有效数据长度
     */
    public int size;

    /**
     * 开头填充字节数
     */
    public int paddingLeft;

    /**
     * 结尾填充字节数
     */
    public int paddingRight;

}
