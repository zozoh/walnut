package org.nutz.walnut.impl.io;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;

public abstract class AbstractBucket implements WnBucket {
    
    protected String _id;

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
    public int write(String s) {
        byte[] bs = s.getBytes(Encoding.CHARSET_UTF8);
        int re = write(0, bs, 0, bs.length);
        this.trancateSize(bs.length);
        return re;
    }

    @Override
    public int append(String s) {
        byte[] bs = s.getBytes(Encoding.CHARSET_UTF8);
        long sz = this.getSize();
        int re = write(sz, bs, 0, bs.length);
        this.trancateSize(sz + bs.length);
        return re;
    }

    @Override
    public int append(byte[] bs, int off, int len) {
        long sz = this.getSize();
        int re = write(sz, bs, off, len);
        this.trancateSize(sz + len - off);
        return re;
    }

    @Override
    public int write(long pos, byte[] bs, int off, int len) {
        int re = 0;
        if (len <= 0) {
            trancateBlock(0);
            return re;
        }

        // 从桶的哪个块开始写
        int block_size = getBlockSize();
        int index = (int) pos / block_size;

        // 第一个块
        int b_first_off = (int) (pos - index * block_size);
        int sz = Math.min(block_size - b_first_off, len);
        re += write(index, b_first_off, bs, off, sz);
        index++;
        len -= sz;
        off += sz;

        // 中间的块
        while (len > block_size) {
            re += write(index, 0, bs, off, block_size);
            index++;
            len -= block_size;
            off += block_size;
        }

        // 最后一个块
        if (len > 0)
            re += write(index, 0, bs, off, len);

        // 返回最后一个操作的桶块下标
        return re;
    }

    protected String _gen_sha1() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");

            WnBucketBlockInfo bi = new WnBucketBlockInfo();
            int b_sz = this.getBlockSize();
            int b_nb = this.getBlockNumber();
            byte[] bs = new byte[b_sz];
            for (int i = 0; i < b_nb; i++) {
                this.read(i, bs, bi);
                md.update(bs, 0, bi.len);
            }

            byte[] hashBytes = md.digest();
            return Lang.fixedHexString(hashBytes);
        }
        catch (NoSuchAlgorithmException e) {
            throw Lang.impossible();
        }
    }

    protected void _assert_index_not_out_of_range(long index) {
        if (index >= getBlockNumber())
            throw Er.create("e.bucket.OutputRange");
    }

    protected void _assert_no_sealed() {
        if (isSealed())
            throw Er.create("e.bucket.sealed");
    }

    public String getId() {
        return _id;
    }
    
    public void setId(String id) {
        this._id = id;
    }
}
