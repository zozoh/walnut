package com.site0.walnut.core.mapping.bm;

import java.util.Map;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.bm.bml.LocalSha1BM;
import com.site0.walnut.core.mapping.WnBMFactory;

public class LocalSha1BMFactory implements WnBMFactory {

    private Map<String, LocalSha1BM> bms;

    public LocalSha1BMFactory() {}

    public void setBms(Map<String, LocalSha1BM> bms) {
        this.bms = bms;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        LocalSha1BM bm = bms.get(str);
        if (null == bm) {
            throw Er.create("e.io.bm.UndefinedLocalBM", str);
        }
        return bm;
    }
    
}
