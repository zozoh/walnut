package com.site0.walnut.seq.impl_id;

import java.util.Date;

import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;

import com.site0.walnut.seq.WnIdGenerator;

public class WnSnowQIdGenerator implements WnIdGenerator {

    private String prefix;
    private StringGenerator sg;

    public WnSnowQIdGenerator(String prefix, int n) {
        this.prefix = prefix;
        this.sg = R.sg(n);
    }

    public WnSnowQIdGenerator(String prefix) {
        this(prefix, 4);
    }

    @Override
    public String next(Date hint) {
        StringBuilder sb = new StringBuilder();
        if (null != prefix && prefix.length() > 0) {
            sb.append(prefix);
        }
        // 生成时间戳
        long ams = hint.getTime();
        sb.append(Long.toString(ams, Character.MAX_RADIX));

        // 生成随机数
        sb.append(sg.next());

        // 返回
        return sb.toString();
    }

}
