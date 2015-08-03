package org.nutz.walnut.impl.io.bucket;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.AbstractBucket;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
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
    private long blockNumber;
    private String fromBucketId;
    private long size;
    private String sha1;

    private List<byte[]> list;

    public MemoryBucket(int blockSize) {
        this.blockSize = blockSize;
        this.list = new ArrayList<byte[]>(5);
    }

    @Override
    public String getSha1() {
        if (null == sha1) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");

                WnBucketBlockInfo bi = new WnBucketBlockInfo();
                for (byte[] bs : list) {
                    Bus.getInfo(bs, bi);
                    md.update(bs, bi.paddingLeft, bi.size);
                }

                byte[] hashBytes = md.digest();
                sha1 = Lang.fixedHexString(hashBytes);
            }
            catch (NoSuchAlgorithmException e) {
                throw Lang.impossible();
            }
        }
        return sha1;
    }

    @Override
    public int read(long index, byte[] bs, WnBucketBlockInfo bi) {
        _assert_index_not_out_of_range(index);

        int i = (int) index;
        byte[] block = i >= list.size() ? null : list.get(i);
        if (null == block)
            Arrays.fill(bs, B0);
        else
            System.arraycopy(block, 0, bs, 0, block.length);
        return Bus.getInfo(block, bi);
    }

    @Override
    public int read(long pos, byte[] bs, int off, int len) {
        int re = 0;
        WnBucketBlockInfo bi = new WnBucketBlockInfo();
        while (pos < len && re < len) {
            // 找到块
            int index = (int) (pos / blockSize);
            _assert_index_not_out_of_range(index);

            // 找到偏移
            int from = (int) (pos - index * blockSize);

            // 读取
            byte[] block = list.get(index);
            int sz = Bus.getInfo(block, bi);
            int n = Math.min(sz, len);
            System.arraycopy(block, bi.paddingLeft + from, bs, off, n);

            // 计数
            re += n;
            len -= n;
            off += n;
            pos += n;

        }
        return re;
    }

    @Override
    public void write(long index, int padding, byte[] bs, int off, int len) {
        _assert_no_sealed();

        // 添加新的桶块
        if (index >= blockNumber) {
            // 补充空余的桶块
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }

            // 追加一个桶块
            byte[] block = new byte[blockSize];
            Bus.fillBlock(block, padding, bs, off, len);
            list.add(block);
            blockNumber = index + 1;
        }
        // 修改已有的桶块
        else {
            byte[] block = new byte[blockSize];
            Bus.fillBlock(block, padding, bs, off, len);
            list.set((int) index, bs);
        }
    }

    @Override
    public void write(long index, byte[] bs) {
        _assert_no_sealed();

        // 添加新的桶块
        if (index >= blockNumber) {
            // 补充空余的桶块
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }

            // 追加一个桶块
            byte[] block = new byte[blockSize];
            Bus.fillBlock(block, 0, bs, 0, bs.length);
            list.add(block);
            blockNumber = index + 1;
        }
        // 修改已有的桶块
        else {
            byte[] block = new byte[blockSize];
            Bus.fillBlock(block, 0, bs, 0, bs.length);
            list.set((int) index, block);
        }
    }

    @Override
    public void trancate(long nb) {
        if (nb == 0) {
            list.clear();
            blockNumber = 0;
            size = 0;
        } else if (nb < list.size()) {
            list = list.subList(0, (int) nb);
            blockNumber = nb;
            size = nb * blockSize;
        }
    }

    @Override
    public String seal() {
        return getSha1();
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

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getFromBucketId() {
        return fromBucketId;
    }

    public void setFromBucketId(String fromBucketId) {
        this.fromBucketId = fromBucketId;
    }

    public boolean isDuplicated() {
        return !Strings.isBlank(fromBucketId);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

}
