package org.nutz.walnut.api.io;

import org.nutz.lang.random.R;

/**
 * 句柄的结构体
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnHandle {

    public String id;
    public long ct;
    public long lm;

    /**
     * @see org.nutz.walnut.util.Wn.S
     */
    public int mode;
    public WnObj obj;

    public long pos;
    public boolean updated;

    public byte[] swap;
    public int swap_size;

    public WnBucket bucket;

    public WnHandle() {
        this.id = R.UU32();
        this.ct = System.currentTimeMillis();
        this.lm = this.ct;
    }

}
