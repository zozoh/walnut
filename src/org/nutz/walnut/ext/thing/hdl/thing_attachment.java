package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqVNHQ", regex = "^(quiet|overwrite|del|ufc)$")
public class thing_attachment implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到集合
        WnObj oTS = Things.checkThingSet(hc.oRefer);

        // 得到对应对 Thing
        WnObj oT = Things.checkThIndex(sys, hc);

        // 得到媒体目录
        WnObj oDir = Things.dirThAttachment(sys.io, oTS, oT);

        // 执行处理
        Things.doFileObj(sys, hc, oDir, oT, "attachment");

    }

}
