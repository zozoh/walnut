package org.nutz.walnut.api.io;

public abstract class WnBucketInfo {

    public String id;
    public boolean sealed;
    public boolean premier;
    public long size;

    public long ct;
    public long lm;
    public long lread;
    public long lsync;
    public long lseal;
    public long lopen;

    public long refer_count;
    public long read_count;

    public int block_size;
    public long block_nb;

}
