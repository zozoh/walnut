package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cnqihbslVNHQ")
public class thing_delete implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oT = Things.checkThing(hc.oHome);

        // 删除
        oT.setv("th_live", Things.TH_DEAD);

        // 标识一下
        sys.io.set(oT, "^th_live$");

        // 记录输出
        hc.output = oT;
    }

}
