package org.nutz.walnut.api.io;

import org.nutz.lang.Encoding;
import org.nutz.walnut.api.err.Er;

public abstract class AbstractBucket implements WnBucket {

    @Override
    public String getString() {
        int size = (int) getSize();
        if (size == 0)
            return "";

        byte[] bs = new byte[size];
        this.read(0, bs, 0, bs.length);
        return new String(bs, Encoding.CHARSET_UTF8);
    }

    @Override
    public void write(String s) {
        byte[] bs = s.getBytes(Encoding.CHARSET_UTF8);
        write(0, bs, 0, bs.length);
    }

    @Override
    public long write(long pos, byte[] bs, int off, int len) {
        if (len <= 0) {
            trancate(0);
            return 0L;
        }

        // 从桶的哪个块开始写
        int block_size = getBlockSize();
        long index = pos / block_size;
        // 桶修改后，有效数据长度，以及一共有多少块
        setSize(pos + len);

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

    protected void _assert_index_not_out_of_range(long index) {
        if (index >= getBlockNumber())
            throw Er.create("e.bucket.OutputRange");
    }

    protected void _assert_no_sealed() {
        if (isSealed())
            throw Er.create("e.bucket.sealed");
    }
}
