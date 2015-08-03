package org.nutz.walnut.impl.io.bucket;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.util.Bus;

public class MemoryBucket extends WnBucket {

    private List<byte[]> list;

    public MemoryBucket(int blockSize) {
        this.block_size = blockSize;
        this.list = new ArrayList<byte[]>(5);
    }

    @Override
    public String sha1() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");

            for (byte[] bs : list) {
                WnBucketBlockInfo bi = Bus.getInfo(bs);
                md.update(bs, bi.paddingLeft, bi.size);
            }

            byte[] hashBytes = md.digest();
            return Lang.fixedHexString(hashBytes);
        }
        catch (NoSuchAlgorithmException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public WnBucketBlockInfo read(long index, byte[] bs) {
        _assert_index_not_out_of_range(index);

        int i = (int) index;
        byte[] block = i >= list.size() ? null : list.get(i);
        if (null == block)
            Arrays.fill(bs, (byte) 0);
        else
            System.arraycopy(block, 0, bs, 0, block.length);
        return Bus.getInfo(block);
    }

    @Override
    public int read(long pos, byte[] bs, int off, int len) {
        int re = 0;
        while (pos < len && re < len) {
            // 找到块
            int index = (int) (pos / block_size);
            _assert_index_not_out_of_range(index);

            // 找到偏移
            int from = (int) (pos - index * block_size);

            // 读取
            byte[] block = list.get(index);
            WnBucketBlockInfo bi = Bus.getInfo(block);
            int n = Math.min(bi.size, len);
            System.arraycopy(block, bi.paddingLeft + from, bs, off, n);

            // 计数
            re += n;
            len -= n;
            off += n;

        }
        return re;
    }

    @Override
    public void write(long index, int padding, byte[] bs, int off, int len) {
        _assert_no_sealed();

        // 添加新的桶块
        if (index >= block_nb) {
            // 补充空余的桶块
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }

            // 追加一个桶块
            byte[] block = new byte[block_size];
            Bus.fillBlock(block, padding, bs, off, len);
            list.add(block);
            block_nb = index + 1;
        }
        // 修改已有的桶块
        else {
            byte[] block = new byte[block_size];
            Bus.fillBlock(block, padding, bs, off, len);
            list.set((int) index, bs);
        }
    }

    @Override
    public void write(long index, byte[] bs) {
        _assert_no_sealed();

        // 添加新的桶块
        if (index >= block_nb) {
            // 补充空余的桶块
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }

            // 追加一个桶块
            byte[] block = new byte[block_size];
            Bus.fillBlock(block, 0, bs, 0, bs.length);
            list.add(block);
            block_nb = index + 1;
        }
        // 修改已有的桶块
        else {
            byte[] block = new byte[block_size];
            Bus.fillBlock(block, 0, bs, 0, bs.length);
            list.set((int) index, block);
        }
    }

    @Override
    public void trancate(long nb) {
        if (nb == 0) {
            list.clear();
            block_nb = 0;
            size = 0;
        } else if (nb < list.size()) {
            list = list.subList(0, (int) nb);
            block_nb = nb;
            size = nb * block_size;
        }
    }

    @Override
    public String seal() {
        return sha1();
    }

    @Override
    public void unseal() {}

    @Override
    public WnBucket duplicate(boolean dropData) {
        throw Lang.noImplement();
    }

    @Override
    public WnBucket margeWith(WnBucket bucket) {
        throw Lang.noImplement();
    }

    @Override
    public long refer() {
        return 0;
    }

    @Override
    public int free() {
        return 0;
    }

}
