package com.site0.walnut.ext.data.thing.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnqVNHQ", regex = "^(del|quiet|overwrite|ufc)$")
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
