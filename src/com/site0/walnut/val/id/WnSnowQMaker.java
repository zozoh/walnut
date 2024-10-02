package com.site0.walnut.val.id;

import java.util.Date;

import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.val.ValueMaker;

public class WnSnowQMaker implements ValueMaker {

    private String prefix;
    private StringGenerator sg;

    public WnSnowQMaker(String prefix, int n) {
        this.prefix = prefix;
        this.sg = R.sg36(n);
    }

    public WnSnowQMaker(String prefix) {
        this(prefix, 4);
    }

    @Override
    public String make(Date hint, NutBean context) {
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
