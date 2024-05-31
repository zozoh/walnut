package com.site0.walnut.val;

import org.nutz.lang.util.NutBean;
import org.nutz.web.WebException;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.val.id.WnSeqDMaker;
import com.site0.walnut.val.id.WnSeqHHMaker;
import com.site0.walnut.val.id.WnSeqIdMaker;
import com.site0.walnut.val.id.WnSnowQDMaker;
import com.site0.walnut.val.id.WnSnowQMaker;
import com.site0.walnut.val.id.WnUU32Maker;
import com.site0.walnut.val.seq.WnObjSeqMaker;
import com.site0.walnut.val.util.WnSeqInfo;

public abstract class ValueMakers {

    public static SeqMaker getSeqMaker(WnSystem sys, WnSeqInfo info) {
        return getSeqMaker(sys.io, sys.session.getVars(), info);
    }

    public static SeqMaker getSeqMaker(WnIo io, NutBean vars, WnSeqInfo info) {
        String path = Wn.normalizeFullPath(info.getScope(), vars);
        WnObj p = io.check(null, path);
        return new WnObjSeqMaker(io, p, info.getTarget(), info.getName());
    }

    public static ValueMaker build(String input, SeqMakerBuilder seqBuilder) {
        String typ = input;
        String val = null;

        // 动态获取
        if (input.startsWith("=")) {
            String key = input.substring(1).trim();
            return new ContextValueMaker(key);
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
        catch (WebException err) {
            return new StaticValueMaker(input);
        }
    }

    public static ValueMaker build(String type, String setup, SeqMakerBuilder seqBuilder) {
        if ("uu32".equalsIgnoreCase(type)) {
            return new WnUU32Maker();
        }

        if (Ws.isBlank(setup)) {
            return new StaticValueMaker(type);
        }

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
