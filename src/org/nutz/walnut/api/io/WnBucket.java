package org.nutz.walnut.api.io;

import org.nutz.lang.Encoding;
import org.nutz.walnut.api.err.Er;

public abstract class WnBucket extends WnBucketInfo {

    public abstract String sha1();

    public String getString() {
        if (size == 0)
            return "";

        byte[] bs = new byte[(int) size];
        this.read(0, bs, 0, bs.length);
        return new String(bs, Encoding.CHARSET_UTF8);
    }

    public void write(String s) {
        byte[] bs = s.getBytes(Encoding.CHARSET_UTF8);
        write(0, bs, 0, bs.length);
    }

    public long write(long pos, byte[] bs, int off, int len) {
        if (len <= 0) {
            trancate(0);
            return 0L;
        }

        // 从桶的哪个块开始写
        long index = pos / block_size;
        // 桶修改后，有效数据长度，以及一共有多少块
        size = pos + len;

        // 第一个块
        int b_first_off = (int) (pos - index * block_size);
        int sz = Math.min(block_size - b_first_off, len);
        this.write(index, b_first_off, bs, off, sz);
        index++;
        len -= sz;
        off += sz;

        // 中间的块
        while (len > block_size) {
            this.write(index, 0, bs, off, block_size);
            index++;
            len -= block_size;
            off += block_size;
        }

        // 最后一个块
        this.write(index, 0, bs, off, len);

        // 返回最后一个操作的桶块下标
        return index;
    }

    /**
     * 读取一个桶块
     * 
     * @param index
     *            桶块的下标，0 Base
     * @param bs
     *            字节数组，长度必须不能小于 block_size，桶块的字节将全部输出到这个数组
     * @return 输出的字节数组的具体布局
     * 
     * @throws "e.bucket.OutputRange"
     *             桶块下标越界
     * 
     * @see WnBucketBlockInfo
     */
    public abstract WnBucketBlockInfo read(long index, byte[] bs);

    /**
     * 从桶中读取一些字节
     * 
     * @param pos
     *            开始读取的字节位置
     * @param bs
     *            输出的字节数组
     * @param off
     *            从输出的字节数组哪里开始写
     * @param len
     *            最多写多少
     * @return 实际读取了多少有效字节
     */
    public abstract int read(long pos, byte[] bs, int off, int len);

    /**
     * @see #write(long, int, byte[], int, int)
     */
    public void write(long index, int padding, byte[] bs, int len) {
        write(index, padding, bs, 0, len);
    }

    /**
     * 写入一个桶块。如果桶块空间还有富余，则用空白填充
     * 
     * @param index
     *            桶块的下标
     * @param padding
     *            桶块开始的空白填充
     * @param bs
     *            字节数组，里面的字节会被写入桶块
     * @param off
     *            从字节数组什么地方开始写
     * @param len
     *            写多长
     */
    public abstract void write(long index, int padding, byte[] bs, int off, int len);

    /**
     * 忠实的安装给定的字节填充桶块
     * 
     * @param index
     *            桶块的下标
     * @param bs
     *            字节数组，里面的字节会被写入桶块，长度等于桶块。
     */
    public abstract void write(long index, byte[] bs);

    /**
     * 剪裁桶的有效数据大小
     * 
     * @param nb
     *            将桶块裁剪到多少个
     */
    public abstract void trancate(long nb);

    public abstract String seal();

    public abstract void unseal();

    /**
     * 复制出一个新桶
     * 
     * @param dropData
     *            复制的时候是否丢掉旧桶的数据
     * 
     * @return 新桶
     */
    public abstract WnBucket duplicate(boolean dropData);

    /**
     * 将另外一个桶的数据合并到当前的桶，本桶的 sha1 等字段会发生响应的改变。
     * <ul>
     * <li>如果原桶的桶块可以与本桶大小不一致
     * <li>本桶的填充字节会被原桶对应的自己替代
     * <ul>
     * 
     * @param bucket
     *            原桶
     * 
     * @return 自身以便链式赋值
     */
    public abstract WnBucket margeWith(WnBucket bucket);

    public abstract long refer();

    /**
     * @return 释放后，桶的引用计数，0 表示这个桶的数据将会被释放
     */
    public abstract int free();

    protected void _assert_index_not_out_of_range(long index) {
        if (index >= block_nb)
            throw Er.create("e.bucket.OutputRange");
    }

    protected void _assert_no_sealed() {
        if (sealed)
            throw Er.create("e.bucket.sealed");
    }
}
