package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqVNHQ", regex = "^(quiet|overwrite|ufc)$")
public class thing_file implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 找到集合
        WnObj oTs = Things.checkThingSet(hc.oRefer);

        // 得到对应对 Thing
        WnObj oT = Things.checkThIndex(sys, hc);

        // 找到操作目录
        String dirPath = hc.params.getString("dir", null);

        // 执行处理
        Things.doFileObj2(sys, hc, oTs, oT, dirPath);
    }

}
