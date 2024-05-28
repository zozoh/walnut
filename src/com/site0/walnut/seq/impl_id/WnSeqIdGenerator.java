package com.site0.walnut.seq.impl_id;

import java.util.Date;

import com.site0.walnut.seq.SeqGenerator;
import com.site0.walnut.seq.WnIdGenerator;
import com.site0.walnut.util.Ws;

public class WnSeqIdGenerator implements WnIdGenerator {

    private String prefix;
    private SeqGenerator seq;
    private int n;

    public WnSeqIdGenerator(String prefix, SeqGenerator seq, int n) {
        this.prefix = prefix;
        this.seq = seq;
        this.n = n;
    }

    public WnSeqIdGenerator(String prefix, SeqGenerator seq) {
        this(prefix, seq, 9);
    }

    @Override
    public String next(Date hint) {
        StringBuilder sb = new StringBuilder();
        if (null != prefix && prefix.length() > 0) {
            sb.append(prefix);
        }

        // 获取序号
        long nb = seq.next(hint);
        String ns = Long.toString(nb);
        String ss = Ws.padStart(ns, n, '0');
        sb.append(ss);

        // 返回
        return sb.toString();
    }

}
