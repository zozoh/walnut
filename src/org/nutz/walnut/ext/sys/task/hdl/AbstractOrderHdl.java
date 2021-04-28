package org.nutz.walnut.ext.sys.task.hdl;

import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public abstract class AbstractOrderHdl extends AbstractTaskModifyHdl {

    protected void _save(WnSystem sys, Map<String, OrderUpdateInfo> map) {
        for (OrderUpdateInfo oui : map.values()) {
            String regex = "^" + Lang.concat("|", oui.keys) + "$";
            sys.io.appendMeta(oui.o, regex);
        }
    }

    protected void _join_obj(Map<String, OrderUpdateInfo> map, WnObj o, String... keys) {
        if (null != o) {
            String id = o.id();
            OrderUpdateInfo oui = map.get(id);
            if (null == oui) {
                oui = new OrderUpdateInfo(o);
                map.put(id, oui);
            }
            for (String key : keys) {
                oui.keys.add(key);
                oui.o.setv(key, o.get(key));
            }
        }
    }

    protected void _set(WnObj o, String key, WnObj ta, String taKey) {
        if (null != o) {
            String taId = null;
            if (null != ta) {
                if (null == taKey)
                    taId = ta.id();
                else
                    taId = ta.getString(taKey);
            }
            o.setv(key, taId);
        }
    }

}
