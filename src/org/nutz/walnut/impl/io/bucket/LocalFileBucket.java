package org.nutz.walnut.impl.io.bucket;

import java.io.File;
import java.io.RandomAccessFile;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.impl.io.AbstractBucket;

public class LocalFileBucket extends AbstractBucket {

    private File f;

    private RandomAccessFile raf;

    private int blockSize;

    private int blockNumber;

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
    public int getBlockNumber() {
        return blockNumber;
    }

    @Override
    public String getParentBucketId() {
        return null;
    }

    @Override
    public void setParentBucketId(String pbid) {}

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
        this.blockNumber = (int) Math.ceil(((double) size) / ((double) blockSize));
    }

    @Override
    public String getSha1() {
        return Lang.sha1(f);
    }

    @Override
    public int read(int index, byte[] bs, WnBucketBlockInfo bi) {

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
    public int write(int index, int padding, byte[] bs, int off, int len) {
        throw Er.create("e.io.bucket.file.readonly", f);
    }

    @Override
    public void trancate(int nb) {
        throw Er.create("e.io.bucket.file.readonly", f);
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
        return new LocalFileBucket(f, this.getBlockSize());
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
