package com.site0.walnut.val;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.val.date.UTCDateMaker;
import com.site0.walnut.val.date.UTCTimestampMaker;
import com.site0.walnut.val.id.WnSeqDMaker;
import com.site0.walnut.val.id.WnSeqHHMaker;
import com.site0.walnut.val.id.WnSeqIdMaker;
import com.site0.walnut.val.id.WnSnowQDMaker;
import com.site0.walnut.val.id.WnSnowQMaker;
import com.site0.walnut.val.id.WnUU32Maker;
import com.site0.walnut.val.seq.WnObjSeqMaker;
import com.site0.walnut.val.util.WnIPv4Maker;
import com.site0.walnut.val.util.WnSeqInfo;

public abstract class ValueMakers {

    public static SeqMaker getSeqMaker(WnSystem sys, WnSeqInfo info) {
        return getSeqMaker(sys.io, sys.session.getEnv(), info);
    }

    public static SeqMaker getSeqMaker(WnIo io, NutBean vars, WnSeqInfo info) {
        String path = Wn.normalizeFullPath(info.getScope(), vars);
        WnObj p = io.check(null, path);
        return new WnObjSeqMaker(io, p, info.getTarget(), info.getName());
    }

    public static ValueMaker build(WnSystem sys, String input) {
        ValueMaker vmk = ValueMakers.build(input, new SeqMakerBuilder() {
            public SeqMaker build(WnSeqInfo info) {
                return ValueMakers.getSeqMaker(sys, info);
            }
        });
        return vmk;
    }

    public static ValueMaker build(String input, SeqMakerBuilder seqBuilder) {
        String typ = input;
        String val = null;

        // 动态获取
        if (input.startsWith("=")) {
            String key = input.substring(1).trim();
            return new ContextValueMaker(key);
        }

        // 静态值
        if (input.startsWith(":")) {
            return new StaticValueMaker(input.substring(1).trim());
        }

        // 获取值的开头
        int pos = input.indexOf(':');
        if (pos > 0) {
            typ = input.substring(0, pos).trim();
            val = input.substring(pos + 1).trim();
        }

        try {
            return build(typ, val, seqBuilder);
        }
        catch (Exception err) {
            return new StaticValueMaker(input);
        }
    }

    public static ValueMaker build(String type, String setup, SeqMakerBuilder seqBuilder) {
        if ("uu32".equalsIgnoreCase(type)) {
            return new WnUU32Maker();
        }

        // UTC 时间字符串
        if ("utc".equals(type)) {
            return new UTCDateMaker(setup);
        }

        // 绝对毫秒数
        else if ("ams".equals(type)) {
            return new UTCTimestampMaker(setup);
        }

        // IPv4
        else if ("ipv4".equals(type)) {
            return new WnIPv4Maker();
        }

        // 当做静态值
        if (Ws.isBlank(setup)) {
            return new StaticValueMaker(type);
        }

        // 剩下的应该就是各种 ID 生成器了
        String input = setup;
        String prefix = null;
        int n = 0;
        int pos = input.indexOf(':');
        if (pos >= 0) {
            prefix = input.substring(0, pos).trim();
            input = input.substring(pos + 1).trim();
        }

        pos = input.indexOf(':');
        if (pos > 0) {
            n = Integer.parseInt(input.substring(0, pos).trim());
            input = input.substring(pos + 1).trim();
        } else {
            n = Integer.parseInt(input);
        }

        if ("snowQ".equals(type)) {
            return new WnSnowQMaker(prefix, n);
        }

        if ("snowQD".equals(type)) {
            return new WnSnowQDMaker(prefix, n);
        }

        // 保护一下
        if (!Ws.isBlank(input)) {
            // 那么就解析序列
            WnSeqInfo seq_info = new WnSeqInfo().valueOf(input);
            SeqMaker seq = seqBuilder.build(seq_info);
            if ("seq".equals(type)) {
                return new WnSeqIdMaker(prefix, seq, n);
            }

            if ("seqD".equals(type)) {
                return new WnSeqDMaker(prefix, seq, n);
            }

            if ("seqHH".equals(type)) {
                return new WnSeqHHMaker(prefix, seq, n);
            }
        }

        // 未知的格式
        throw Er.createf("e.val.make.Fail : %s = %s", type, setup);
    }
}
