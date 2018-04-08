package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.ext.thing.impl.DeleteThingAction;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(quiet)$")
public class thing_delete implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        DeleteThingAction TA = new DeleteThingAction();
        TA.setIo(sys.io).setThingSet(hc.oRefer);
        TA.setIds(hc.params.vals);
        hc.output = TA.invoke();

    }

}
