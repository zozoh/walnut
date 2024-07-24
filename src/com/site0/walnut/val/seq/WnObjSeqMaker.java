package com.site0.walnut.val.seq;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.SeqMaker;

public class WnObjSeqMaker implements SeqMaker {

    private WnIo io;
    private WnObj p;
    private String format;
    private String key;

    public WnObjSeqMaker(WnIo io, WnObj p, String format, String key) {
        this.io = io;
        this.p = p;
        this.format = format;
        this.key = key;
    }

    @Override
    public long make(Date hint, NutBean context) {
        String fname = Wtime.format(hint, format);
        WnObj o = io.createIfNoExists(p, fname, WnRace.FILE);
        // 默认3日过期
        if (o.expireTime() <= 0) {
            // TODO 根据 format 自行决定默认的过期时间
            
            //o.expireTime(System.currentTimeMillis() + 86400000L * 3);
        }
        return io.inc(o.id(), key, 1, true);
    }

}
