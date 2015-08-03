package org.nutz.walnut.impl.io.bucket;

import java.io.File;
import java.io.RandomAccessFile;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.AbstractBucket;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;

public class LocalFileBucket extends AbstractBucket {

    private File f;

    private RandomAccessFile raf;

    private int blockSize;

    private long blockNumber;

    @Override
    public String getId() {
        return "file://" + f.getAbsolutePath();
    }

    @Override
    public boolean isSealed() {
        return false;
    }

    @Override
    public long getCreateTime() {
        return f.lastModified();
    }

    @Override
    public long getLastModified() {
        return f.lastModified();
    }

    @Override
    public long getLastReaded() {
        return f.lastModified();
    }

    @Override
    public long getLastWrited() {
        return f.lastModified();
    }

    @Override
    public long getLastSealed() {
        return f.lastModified();
    }

    @Override
    public long getLastOpened() {
        return f.lastModified();
    }

    @Override
    public long getCountRefer() {
        return 1;
    }

    @Override
    public long getCountRead() {
        return 1;
    }

    @Override
    public int getBlockSize() {
        return blockSize;
    }

    @Override
    public long getBlockNumber() {
        return blockNumber;
    }

    @Override
    public String getFromBucketId() {
        return null;
    }

    @Override
    public void setFromBucketId(String buid) {}

    @Override
    public boolean isDuplicated() {
        return false;
    }

    @Override
    public void setSize(long size) {
        throw Lang.noImplement();
    }

    @Override
    public long getSize() {
        return f.length();
    }

    public LocalFileBucket(File f, int blockSize) {
        this.f = f;
        long size = f.length();
        this.blockSize = 8192;
        this.blockNumber = (long) Math.ceil(((double) size) / ((double) blockSize));
    }

    @Override
    public String getSha1() {
        return Lang.sha1(f);
    }

    @Override
    public int read(long index, byte[] bs, WnBucketBlockInfo bi) {

        long pos = index * getBlockSize();
        int pl = 0;
        int sz = read(pos, bs, 0, bs.length);
        int pr = bs.length - sz;

        if (null != bi)
            bi.set(pl, sz, pr);

        return sz;
    }

    @Override
    public int read(long pos, byte[] bs, int off, int len) {
        try {
            if (null == raf) {
                raf = new RandomAccessFile(f, "r");
            }
            if (raf.getFilePointer() != pos)
                raf.seek(pos);
            return raf.read(bs, off, len);
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    public void write(long index, int padding, byte[] bs, int off, int len) {
        throw Er.create("e.io.bucket.file.readonly", f);
    }

    @Override
    public void write(long index, byte[] bs) {
        throw Er.create("e.io.bucket.file.readonly", f);
    }

    @Override
    public void trancate(long nb) {
        throw Er.create("e.io.bucket.file.readonly", f);
    }

    @Override
    public String seal() {
        return getSha1();
    }

    @Override
    public void unseal() {}

    @Override
    public WnBucket duplicate(boolean dropData) {
        return new LocalFileBucket(f, this.getBlockSize());
    }

    @Override
    public WnBucket margeWith(WnBucket bucket) {
        throw Er.create("e.io.bucket.file.readonly", f);
    }

    @Override
    public long refer() {
        return 1;
    }

    @Override
    public long free() {
        return 1;
    }

}
