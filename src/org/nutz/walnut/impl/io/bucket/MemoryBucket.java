package org.nutz.walnut.impl.io.bucket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.impl.io.AbstractBucket;
import org.nutz.walnut.util.Bus;

public class MemoryBucket extends AbstractBucket {

    private static final byte B0 = (byte) 0;

    private String id;
    private boolean sealed;
    private long createTime;
    private long lastModified;
    private long lastReaded;
    private long lastWrited;
    private long lastSealed;
    private long lastOpened;
    private long countRefer;
    private long countRead;
    private int blockSize;
    private int blockNumber;
    private long size;
    private String sha1;

    private List<byte[]> list;

    public MemoryBucket(int blockSize) {
        this.blockSize = blockSize;
        this.list = new ArrayList<byte[]>(5);
    }

    @Override
    public String getSha1() {
        if (null == sha1)
            sha1 = _gen_sha1();

        return sha1;
    }

    @Override
    public int read(int index, byte[] bs, WnBucketBlockInfo bi) {
        _assert_index_not_out_of_range(index);

        byte[] block = index >= list.size() ? null : list.get(index);
        if (null == block)
            Arrays.fill(bs, B0);
        else
            System.arraycopy(block, 0, bs, 0, block.length);

        // 计数
        this.countRead++;

        // 获得信息并返回
        return Bus.evalInfo(block, bi);

    }

    @Override
    public int read(long pos, byte[] bs, int off, int len) {
        int re = 0;
        WnBucketBlockInfo bi = new WnBucketBlockInfo();
        while (pos < size && len > 0) {
            // 找到块
            int index = (int) (pos / blockSize);

            // 找到偏移
            int from = (int) (pos - index * blockSize);

            // 读取
            byte[] block = list.get(index);

            int n;
            // 空块
            if (null == block) {
                n = Math.min(blockSize - from, len);
            }
            // 有内容，分析一下
            else {
                Bus.evalInfo(block, bi);
                int pos0 = Math.max(from, bi.paddingLeft);
                n = Math.min(len, blockSize - bi.paddingRight - pos0);
                System.arraycopy(block, pos0, bs, off, n);
            }
            // 计数
            re += n;
            len -= n;
            off += n;
            pos += n;

        }

        // 计数
        this.countRead++;

        // 返回读取的有效字节数
        return re;
    }

    @Override
    public int write(int index, int padding, byte[] bs, int off, int len) {
        _assert_no_sealed();

        int n;

        // 添加新的桶块
        if (index >= blockNumber) {
            // 补充空余的桶块
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }

            // 追加一个桶块
            byte[] block = new byte[blockSize];
            list.add(block);

            // 最多填充多少字节
            n = Math.min(blockSize - padding, len);

            // 填充
            System.arraycopy(bs, off, block, padding, n);

            // 计算结果
            blockNumber = index + 1;
            size = index * blockSize + n;
        }
        // 修改已有的桶块
        else {
            byte[] block = list.get(index);
            // 确保桶块不为 null
            if (null == block) {
                block = new byte[blockSize];
                list.set(index, block);
            }
            // 最多填充多少字节
            n = Math.min(blockSize - padding, len);

            // 填充
            System.arraycopy(bs, off, block, padding, n);

            // 如果是最后一个桶块 ...
            if (index == blockNumber - 1) {
                WnBucketBlockInfo bi = Bus.getInfo(block);
                size = index * blockSize - bi.paddingRight;
            }
        }

        // 删除指纹缓冲
        sha1 = null;

        // 返回实际写入的字节数
        return n;
    }

    @Override
    public void trancateBlock(int nb) {
        if (nb == blockNumber) {
            return;
        }

        // 清零
        if (nb == 0) {
            list.clear();
            blockNumber = 0;
        }
        // 剪裁
        else if (nb < list.size()) {
            list = list.subList(0, (int) nb);
            blockNumber = nb;
        }
        // 清除指纹
        sha1 = null;
    }

    @Override
    public String seal() {
        return getSha1();
    }

    @Override
    public void unseal() {}

    @Override
    public void update() {}

    @Override
    public WnBucket duplicateVirtual() {
        throw Lang.noImplement();
    }

    @Override
    public long refer() {
        return 1;
    }

    @Override
    public long free() {
        return 1;
    }

    public String getId() {
        return id;
    }

    public boolean isSealed() {
        return sealed;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLastReaded() {
        return lastReaded;
    }

    public long getLastWrited() {
        return lastWrited;
    }

    public long getLastSealed() {
        return lastSealed;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public long getCountRefer() {
        return countRefer;
    }

    public long getCountRead() {
        return countRead;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public String getParentBucketId() {
        throw Lang.noImplement();
    }

    public void setParentBucketId(String pbid) {
        throw Lang.noImplement();
    }

    public boolean isDuplicated() {
        return false;
    }

    public long getSize() {
        return size;
    }

    public void trancateSize(long size) {
        if (this.size == size)
            return;

        // 应该有多少块
        long b_nb = (long) Math.ceil(((double) size) / ((double) blockSize));

        // 补充空块
        for (long i = blockNumber; i < b_nb; i++)
            list.add(null);

        // 裁剪
        if (b_nb < blockNumber) {
            list = list.subList(0, (int) blockNumber);
        }

        // 保存有效尺寸
        this.size = size;

        // 清除指纹
        this.sha1 = null;
    }

}
