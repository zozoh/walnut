package org.nutz.walnut.impl.io.bucket;

import java.io.File;
import java.io.RandomAccessFile;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.api.io.WnBucket;

public class LocalFileBucket extends WnBucket {

    private File f;

    private RandomAccessFile raf;

    public LocalFileBucket(File f) {
        this.f = f;
        this.id = "file://" + f.getAbsolutePath();
        this.sealed = false;
        this.premier = false;
        this.size = f.length();

        this.ct = f.lastModified();
        this.lm = f.lastModified();
        this.lread = System.currentTimeMillis();
        this.lsync = f.lastModified();
        this.lseal = f.lastModified();
        this.lopen = this.lread;

        this.refer_count = 1;
        this.read_count = 1;

        this.block_size = 8192;
        this.block_nb = (long) Math.ceil(((double) size) / ((double) block_size));
    }

    @Override
    public String sha1() {
        return Lang.sha1(f);
    }

    @Override
    public WnBucketBlockInfo read(long index, byte[] bs) {
        WnBucketBlockInfo re = new WnBucketBlockInfo();

        long pos = index * block_size;
        re.paddingLeft = 0;
        re.size = read(pos, bs, 0, bs.length);
        re.paddingRight = bs.length - re.size;

        return re;
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
        return sha1();
    }

    @Override
    public void unseal() {}

    @Override
    public WnBucket duplicate(boolean dropData) {
        return new LocalFileBucket(f);
    }

    @Override
    public WnBucket margeWith(WnBucket bucket) {
        throw Er.create("e.io.bucket.file.readonly", f);
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
