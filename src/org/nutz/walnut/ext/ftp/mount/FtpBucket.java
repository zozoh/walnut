package org.nutz.walnut.ext.ftp.mount;

import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketBlockInfo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.AbstractBucket;

public class FtpBucket extends AbstractBucket {
    
    protected WnObj wobj;

    public FtpBucket(WnObj wobj) {
        this.wobj = wobj;
    }

    public boolean isSealed() {
        return false;
    }

    public long getCreateTime() {
        return 0;
    }

    public long getLastModified() {
        return 0;
    }

    public long getLastReaded() {
        return 0;
    }

    public long getLastWrited() {
        return 0;
    }

    public long getLastSealed() {
        return 0;
    }

    public long getLastOpened() {
        return 0;
    }

    public long getCountRefer() {
        return 1;
    }

    public long getCountRead() {
        return 0;
    }

    public int getBlockSize() {
        return 0;
    }

    public int getBlockNumber() {
        return 0;
    }

    public String getParentBucketId() {
        return null;
    }

    public void setParentBucketId(String pbid) {
    }

    public boolean isDuplicated() {
        return false;
    }

    public long getSize() {
        return 0;
    }

    public String getSha1() {
        return null;
    }

    public int read(int index, byte[] bs, WnBucketBlockInfo bi) {
        return 0;
    }

    public int read(long pos, byte[] bs, int off, int len) {
        return 0;
    }

    public int write(int index, int padding, byte[] bs, int off, int len) {
        return 0;
    }

    public void trancateBlock(int nb) {
    }

    public void trancateSize(long size) {
    }

    public String seal() {
        return null;
    }

    public void unseal() {
        
    }

    public void update() {
    }

    public WnBucket duplicateVirtual() {
        return new FtpBucket(this.wobj);
    }
    public long refer() {
        return 1;
    }

    public long free() {
        return 1;
    }

}
