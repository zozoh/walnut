package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(quiet)$")
public class thing_delete implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oT = Things.checkThing(hc.oRefer);

        // 已经是删除的了
        if (oT.getInt("th_live", 0) == Things.TH_DEAD) {
            if (!hc.params.is("quiet")) {
                throw Er.create("e.cmd.thing.delete.already", oT.id());
            }
        }
        // 执行删除
        else {
            oT.setv("th_live", Things.TH_DEAD);
            sys.io.set(oT, "^th_live$");
        }

        // 记录输出
        hc.output = oT;
    }

}
