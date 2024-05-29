package com.site0.walnut.seq;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.seq.impl_id.WnSeqDMaker;
import com.site0.walnut.seq.impl_id.WnSeqHHMaker;
import com.site0.walnut.seq.impl_id.WnSeqIdMaker;
import com.site0.walnut.seq.impl_id.WnSnowQDMaker;
import com.site0.walnut.seq.impl_id.WnSnowQMaker;
import com.site0.walnut.seq.impl_id.WnUU32Maker;
import com.site0.walnut.seq.impl_seq.WnObjSeqMaker;
import com.site0.walnut.util.Wn;

public abstract class WnIds {

    public static SeqMaker getSeqMaker(WnIo io,
                                       NutBean vars,
                                       String parentPath,
                                       String name,
                                       String key) {
        String path = Wn.normalizeFullPath(parentPath, vars);
        WnObj p = io.check(null, path);
        return new WnObjSeqMaker(io, p, name, key);
    }

    public String seqD(String prefix, SeqMaker seq, int n) {
        WnSeqDMaker g = new WnSeqDMaker(prefix, seq, n);
        return g.make(new Date());
    }

    public String seqHH(String prefix, SeqMaker seq, int n) {
        WnSeqHHMaker g = new WnSeqHHMaker(prefix, seq, n);
        return g.make(new Date());
    }

    public String seq(String prefix, SeqMaker seq, int n) {
        WnSeqIdMaker g = new WnSeqIdMaker(prefix, seq, n);
        return g.make(new Date());
    }

    public String snowQ(String prefix, int n) {
        WnSnowQMaker g = new WnSnowQMaker(prefix, n);
        return g.make(new Date());
    }

    public String snowQD(String prefix, int n) {
        WnSnowQDMaker g = new WnSnowQDMaker(prefix, n);
        return g.make(new Date());
    }

    public String UU32() {
        WnUU32Maker g = new WnUU32Maker();
        return g.make(new Date());
    }
}
