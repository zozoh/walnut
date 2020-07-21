package org.nutz.walnut.core.mapping.bm;

import java.util.Map;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.bm.localbm.LocalIoBM;
import org.nutz.walnut.core.mapping.WnBMFactory;

public class LocalIoBMFactory implements WnBMFactory {

    private Map<String, LocalIoBM> bms;

    @Override
    public WnIoBM load(String homeId, String str) {
        LocalIoBM bm = bms.get(str);
        if (null == bm) {
            throw Er.create("e.io.bm.UndefinedLocalBM", str);
        }
        return bm;
    }

}
