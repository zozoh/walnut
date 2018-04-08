package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.GetThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cnqihbslVNHQ")
public class thing_get implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 得到对应对 Thing
        WnObj oTs = Things.checkThingSet(hc.oRefer);

        GetThingAction TA = new GetThingAction();
        TA.setIo(sys.io).setThingSet(oTs);
        String thId = hc.params.val_check(0);
        TA.setId(thId).setFull(hc.params.is("full"));
        hc.output = TA.invoke();

    }

}
