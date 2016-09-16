package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(quiet)$")
public class thing_restore implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oT = Things.checkThing(hc.oRefer);

        // 已经是恢复的了
        if (oT.getInt("th_live", 0) == Things.TH_LIVE) {
            if (!hc.params.is("quiet")) {
                throw Er.create("e.cmd.thing.restore.already", oT.id());
            }
        }
        // 执行恢复
        else {
            oT.setv("th_live", Things.TH_LIVE);
            sys.io.set(oT, "^th_live$");
        }

        // 记录输出
        hc.output = oT;
    }

}
