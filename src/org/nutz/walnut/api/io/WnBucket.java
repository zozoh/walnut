package org.nutz.walnut.api.io;

public abstract class WnBucket {

    public abstract String sha1(boolean gen);

    public abstract int read(long pos, byte[] bs);

    public abstract int read(long pos, byte[] bs, int off, int len);

    public abstract void write(long pos, byte[] bs);

    public abstract void write(long pos, byte[] bs, int off, int len);

    /**
     * 剪裁桶的有效数据大小
     * 
     * @param size
     *            桶的新大小，如果为 0 相当于 free
     */
    public abstract void trancate(long size);

    public abstract String seal();

    public abstract void unseal();

    /**
     * 复制出一个一模一样的新桶
     * 
     * @return 新桶
     */
    public abstract WnBucket duplicate();

    /**
     * 将另外一个桶的数据合并到当前的桶，本桶的 sha1 等字段会发生响应的改变
     * 
     * @param bucket
     *            源桶
     * 
     * @return 自身以便链式赋值
     */
    public abstract WnBucket margeWith(WnBucket bucket);

    public abstract long refer();

    /**
     * @return 释放后，桶的引用计数，0 表示这个桶的数据将会被释放
     */
    public abstract int free();

    /*--------------------------------------------------------*/
    /* 下面是子类用的一些成员变量 */
    /*--------------------------------------------------------*/

    protected String id;
    protected boolean sealed;
    protected String sha1;
    protected long len;
    protected long padding;

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
