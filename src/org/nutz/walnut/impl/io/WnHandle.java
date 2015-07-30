package org.nutz.walnut.impl.io;

import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnObj;

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
     * @see #R
     * @see #W
     * @see #RW
     */
    public int mode;

    public static final int R = 0;
    public static final int W = 1;
    public static final int RW = R | W;

    public WnObj obj;
    public long offset;
    public long pos;
    public byte[] swap;
    public int swap_size;
    public WnBucket bucket;

}
