package com.site0.walnut.seq.impl_id;

import java.util.Date;

import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;

import com.site0.walnut.seq.WnIdGenerator;
import com.site0.walnut.util.Wtime;

public class WnSnowQDIdGenerator implements WnIdGenerator {

    private String prefix;
    private StringGenerator sg;

    public WnSnowQDIdGenerator(String prefix, int n) {
        this.prefix = prefix;
        this.sg = R.sg(n);
    }

    public WnSnowQDIdGenerator(String prefix) {
        this(prefix, 4);
    }

    @Override
    public String next(Date hint) {
        StringBuilder sb = new StringBuilder();
        if (null != prefix && prefix.length() > 0) {
            sb.append(prefix);
        }
        // 生成时间戳
        String dst = Wtime.format(hint, "yyMMddHHmmssSSS");
        sb.append(dst);

        // 生成随机数
        sb.append(sg.next());

        // 返回
        return sb.toString();
    }

}
