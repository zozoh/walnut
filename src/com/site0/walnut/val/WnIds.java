package com.site0.walnut.val;

import java.util.Date;

import com.site0.walnut.val.id.WnSeqDMaker;
import com.site0.walnut.val.id.WnSeqHHMaker;
import com.site0.walnut.val.id.WnSeqIdMaker;
import com.site0.walnut.val.id.WnSnowQDMaker;
import com.site0.walnut.val.id.WnSnowQMaker;
import com.site0.walnut.val.id.WnUU32Maker;

public abstract class WnIds {

    public String seqD(String prefix, SeqMaker seq, int n) {
        WnSeqDMaker g = new WnSeqDMaker(prefix, seq, n);
        return g.make(new Date(), null);
    }

    public String seqHH(String prefix, SeqMaker seq, int n) {
        WnSeqHHMaker g = new WnSeqHHMaker(prefix, seq, n);
        return g.make(new Date(), null);
    }

    public String seq(String prefix, SeqMaker seq, int n) {
        WnSeqIdMaker g = new WnSeqIdMaker(prefix, seq, n);
        return g.make(new Date(), null);
    }

    public String snowQ(String prefix, int n) {
        WnSnowQMaker g = new WnSnowQMaker(prefix, n);
        return g.make(new Date(), null);
    }

    public String snowQD(String prefix, int n) {
        WnSnowQDMaker g = new WnSnowQDMaker(prefix, n);
        return g.make(new Date(), null);
    }

    public String UU32() {
        WnUU32Maker g = new WnUU32Maker();
        return g.make(new Date(), null);
    }
}
