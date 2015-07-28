package org.nutz.walnut.api.io;

public abstract class WnBucket {

    public abstract String sha1(boolean gen);

    public abstract int read(long pos, byte[] bs);

    public abstract int read(long pos, byte[] bs, int off, int len);

    public abstract void write(long pos, byte[] bs);

    public abstract void write(long pos, byte[] bs, int off, int len);

    public abstract String seal();

    public abstract void unseal();

    public abstract WnBucket duplicate();

    public abstract long refer();

    /*--------------------------------------------------------*/
    /* 下面是子类用的一些成员变量 */
    /*--------------------------------------------------------*/

    protected String id;
    protected boolean sealed;
    protected String sha1;
    protected long len;

    protected long ct;
    protected long lm;
    protected long lread;
    protected long lsync;
    protected long lseal;
    protected long lopen;

    protected long refer_count;
    protected long read_count;

    protected int block_size;
    protected int block_nb;

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public boolean isSealed() {
        return sealed;
    }

    public long len() {
        return len;
    }

    public long ct() {
        return ct;
    }

    public long lm() {
        return lm;
    }

    public long lread() {
        return lread;
    }

    public long lsync() {
        return lsync;
    }

    public long lseal() {
        return lseal;
    }

    public long lopen() {
        return lopen;
    }

    public long refer_count() {
        return refer_count;
    }

    public long read_count() {
        return read_count;
    }

    public int block_size() {
        return block_size;
    }

    public int block_nb() {
        return block_nb;
    }

}
