package com.site0.walnut.seq;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.seq.impl_id.WnSeqDIdGenerator;
import com.site0.walnut.seq.impl_id.WnSeqHHIdGenerator;
import com.site0.walnut.seq.impl_id.WnSeqIdGenerator;
import com.site0.walnut.seq.impl_id.WnSnowQDIdGenerator;
import com.site0.walnut.seq.impl_id.WnSnowQIdGenerator;
import com.site0.walnut.seq.impl_id.WnUU32IdGenerator;
import com.site0.walnut.seq.impl_seq.WnObjSeqGenerator;
import com.site0.walnut.util.Wn;

public abstract class WnIds {

    public static SeqGenerator getSeq(WnIo io,
                                      NutBean vars,
                                      String parentPath,
                                      String name,
                                      String key) {
        String path = Wn.normalizeFullPath(parentPath, vars);
        WnObj p = io.check(null, path);
        return new WnObjSeqGenerator(io, p, name, key);
    }

    public String seqD(String prefix, SeqGenerator seq, int n) {
        WnSeqDIdGenerator g = new WnSeqDIdGenerator(prefix, seq, n);
        return g.next(new Date());
    }

    public String seqHH(String prefix, SeqGenerator seq, int n) {
        WnSeqHHIdGenerator g = new WnSeqHHIdGenerator(prefix, seq, n);
        return g.next(new Date());
    }

    public String seq(String prefix, SeqGenerator seq, int n) {
        WnSeqIdGenerator g = new WnSeqIdGenerator(prefix, seq, n);
        return g.next(new Date());
    }

    public String snow(String prefix, int n) {
        WnSnowQIdGenerator g = new WnSnowQIdGenerator(prefix, n);
        return g.next(new Date());
    }

    public String snowQ(String prefix, int n) {
        WnSnowQDIdGenerator g = new WnSnowQDIdGenerator(prefix, n);
        return g.next(new Date());
    }

    public String UU32() {
        WnUU32IdGenerator g = new WnUU32IdGenerator();
        return g.next(new Date());
    }
}
