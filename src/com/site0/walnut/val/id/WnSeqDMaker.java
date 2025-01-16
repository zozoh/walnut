package com.site0.walnut.val.id;

import java.util.Calendar;
import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.SeqMaker;
import com.site0.walnut.val.ValueMaker;

public class WnSeqDMaker implements ValueMaker {

    private String prefix;
    private SeqMaker seq;

    /**
     * 尾部序号的位数：
     * <ul>
     * <li>7 : 每日最多1千万条
     * <li>9 : 每日最多10亿条
     * </ul>
     */
    private int n;

    public WnSeqDMaker(String prefix, SeqMaker seq, int n) {
        this.prefix = prefix;
        this.seq = seq;
        this.n = n;
    }

    public WnSeqDMaker(String prefix, SeqMaker seq) {
        this(prefix, seq, 9);
    }

    @Override
    public String make(Date hint, NutBean context) {
        StringBuilder sb = new StringBuilder();
        if (null != prefix && prefix.length() > 0) {
            sb.append(prefix);
        }

        // 生成时间戳
        Calendar now = Calendar.getInstance();
        String dst = Wtime.formatUTC(now, "yyMMdd");
        sb.append(dst);

        // 获取序号
        long nb = seq.make(hint, context);
        String ns = Long.toString(nb);
        String ss = Ws.padStart(ns, n, '0');
        sb.append(ss);

        // 返回
        return sb.toString();
    }

}
