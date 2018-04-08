package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.ext.thing.impl.UpdateThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqVNHQ", regex = "^(quiet|overwrite)$")
public class thing_update implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        UpdateThingAction TA = new UpdateThingAction();
        TA.setIo(sys.io).setThingSet(hc.oRefer);
        TA.setId(hc.params.val_check(0));
        TA.setName(hc.params.val(1));
        TA.setMeta(Things.fillMeta(sys, hc.params));
        hc.output = TA.invoke();
    }

}
