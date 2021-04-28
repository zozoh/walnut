package org.nutz.walnut.ext.data.thing.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(quiet)$")
public class thing_restore implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        List<WnObj> list = new ArrayList<>(hc.params.vals.length);

        for (String id : hc.params.vals) {
            // 得到对应对 Thing
            WnObj oT = Things.checkThIndex(sys.io, hc.oRefer, id);

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
                list.add(oT);
            }
        }

        // 记录输出
        hc.output = list;
    }

}
