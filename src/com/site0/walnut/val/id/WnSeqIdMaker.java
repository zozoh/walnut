package com.site0.walnut.val.id;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.val.SeqMaker;
import com.site0.walnut.val.ValueMaker;

public class WnSeqIdMaker implements ValueMaker {

    private String prefix;
    private SeqMaker seq;
    private int n;

    public WnSeqIdMaker(String prefix, SeqMaker seq, int n) {
        this.prefix = prefix;
        this.seq = seq;
        this.n = n;
    }

    public WnSeqIdMaker(String prefix, SeqMaker seq) {
        this(prefix, seq, 9);
    }

    @Override
    public String make(Date hint, NutBean context) {
        StringBuilder sb = new StringBuilder();
        if (null != prefix && prefix.length() > 0) {
            sb.append(prefix);
        }

        // 获取序号
        long nb = seq.make(hint, context);
        String ns = Long.toString(nb);
        String ss = Ws.padStart(ns, n, '0');
        sb.append(ss);

        // 返回
        return sb.toString();
    }

}
