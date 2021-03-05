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
            // 看看是否是 xxx=id 这种形式的键
            boolean asId = false;
            if (key.endsWith("=id")) {
                asId = true;
                key = key.substring(0, key.length() - 3);
            }
            String store = storeKeyTmpl.render(Lang.map("key", key));
            __load_refer_by(o, key, asId, store);
        }
    }

    void __load_refer_by(WnObj o, String key, boolean asId, String store) {
        String val = o.getString(key);
        WnObj oRefer = null;
        if (!Ws.isBlank(val)) {
            if (asId) {
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
