package org.nutz.walnut.impl.io;

import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnObj;

public abstract class WnHandle {

    /*--------------------------------------------------------*/
    /* 下面是子类用的一些成员变量 */
    /*--------------------------------------------------------*/
    protected String id;
    protected long ct;
    protected long lm;
    /**
     * @see #R
     * @see #W
     * @see #RW
     */
    protected int mode;

    public static final int R = 0;
    public static final int W = 1;
    public static final int RW = R | W;

    protected WnObj obj;
    protected long offset;
    protected long posr;
    protected long posw;
    protected WnBucket swap;

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public long ct() {
        return ct;
    }

    public void ct(long ct) {
        this.ct = ct;
    }

    public long lm() {
        return lm;
    }

    public void lm(long lm) {
        this.lm = lm;
    }

    public int mode() {
        return mode;
    }

    public void mode(int mode) {
        this.mode = mode;
    }

    public boolean isMode(int mask) {
        return (mode & mask) > 0;
    }

    public boolean isModeStrict(int mask) {
        return 0 == ~((~mask) | mode);
    }

    public boolean isR() {
        return isMode(R);
    }

    public boolean isOnlyR() {
        return isModeStrict(R);
    }

    public boolean isRW() {
        return isModeStrict(R);
    }

    public boolean isOnlyW() {
        return isModeStrict(W);
    }

    public WnObj obj() {
        return obj;
    }

    public void obj(WnObj obj) {
        this.obj = obj;
    }

    public long offset() {
        return offset;
    }

    public void offset(long off) {
        this.offset = off;
    }

    public long posr() {
        return posr;
    }

    public void posr(long posr) {
        this.posr = posr;
    }

    public long posw() {
        return posw;
    }

    public void posw(long posw) {
        this.posw = posw;
    }

    public WnBucket swap() {
        return swap;
    }

    public void swap(WnBucket swap) {
        this.swap = swap;
    }

}
