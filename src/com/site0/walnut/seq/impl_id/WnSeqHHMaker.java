package com.site0.walnut.seq.impl_id;

import java.util.Date;

import com.site0.walnut.seq.SeqMaker;
import com.site0.walnut.seq.IDMaker;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public class WnSeqHHMaker implements IDMaker {

    private String prefix;
    private SeqMaker seq;
    private int n;

    public WnSeqHHMaker(String prefix, SeqMaker seq, int n) {
        this.prefix = prefix;
        this.seq = seq;
        this.n = n;
    }

    public WnSeqHHMaker(String prefix, SeqMaker seq) {
        this(prefix, seq, 5);
    }

    @Override
    public String make(Date hint) {
        StringBuilder sb = new StringBuilder();
        if (null != prefix && prefix.length() > 0) {
            sb.append(prefix);
        }

        // 生成时间戳
        String dst = Wtime.format(hint, "yyMMddHH");
        sb.append(dst);

        // 获取序号
        long nb = seq.make(hint);
        String ns = Long.toString(nb);
        String ss = Ws.padStart(ns, n, '0');
        sb.append(ss);

        // 返回
        return sb.toString();
    }

}
