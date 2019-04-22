package org.nutz.walnut.ext.esi;

import java.io.ByteArrayOutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.hook.WnHookBreak;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.hook.AbstractWnHook;

public class EsiHook extends AbstractWnHook {
    
    protected ElasticsearchService service;
    
    protected String opType;
    
    public EsiHook(ElasticsearchService service, String opType) {
        this.service = service;
        this.opType = opType;
    }

    public void invoke(WnHookContext hc, WnObj wobj) throws WnHookBreak {
        if (wobj.isDIR())
            return;
        EsiConf conf = findConf(wobj, wobj);
        if (conf == null)
            return;
        switch (opType) {
        case "create":
        case "meta":
            service.addOrUpdateMeta(conf, wobj);
            break;
        case "delete":
            service.delete(wobj.creator(), conf, wobj.id());
            break;
        case "write":
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            hc.io().readAndClose(wobj, bao);
            service.updateData(conf, wobj, new String(bao.toByteArray()));
            break;
        default:
            break;
        }
    }
    
    public EsiConf findConf(WnObj wobj, WnObj origin) {
        NutMap conf = wobj.getAs("esi_conf", NutMap.class);
        if (conf != null && wobj.getBoolean("esi_enable", true)) {
            if (!conf.getBoolean("recu") || origin.parentId().equals(wobj.id())) {
                return Lang.map2Object(conf, EsiConf.class);
            }
        }
        wobj = wobj.parent();
        if (wobj != null)
            return findConf(wobj, origin);
        return null;
    }

    public String getType() {
        return "esi";
    }

    protected void _init(String text) {
    }

}
