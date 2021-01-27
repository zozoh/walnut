package org.nutz.walnut.ext.o.impl;

import org.nutz.lang.Lang;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.validate.WnMatch;

public class WnReferLoader {

    private WnIo io;

    private boolean asId;

    private String[] referKeys;

    private String storeKey;

    private WnMatch keyMatch;

    public WnReferLoader(WnIo io) {
        this.io = io;
    }

    public void loadRefer(WnObj o) {
        if (null == referKeys || referKeys.length == 0) {
            return;
        }
        Tmpl storeKeyTmpl = Tmpl.parse(storeKey);
        for (String key : referKeys) {
            String store = storeKeyTmpl.render(Lang.map("key", key));
            __load_refer_by(o, key, store);
        }
    }

    void __load_refer_by(WnObj o, String key, String store) {
        String val = o.getString(key);
        WnObj oRefer = null;
        if (!Ws.isBlank(val)) {
            if (this.asId) {
                oRefer = io.get(val);
            } else {
                oRefer = io.fetch(o, val);
            }
        }
        // 过滤字段
        if (null != oRefer) {
            NutBean bean = Wobj.filterObjKeys(oRefer, keyMatch);
            o.put(store, bean);
        }
    }

    public void setAsId(boolean asId) {
        this.asId = asId;
    }

    public void setReferKey(String str) {
        this.referKeys = Ws.splitIgnoreBlank(str);
    }

    public void setReferKeys(String[] referKeys) {
        this.referKeys = referKeys;
    }

    public void setStoreKey(String storeKey) {
        this.storeKey = storeKey;
    }

    public void setKeyMatch(WnMatch keyMatch) {
        this.keyMatch = keyMatch;
    }

}
