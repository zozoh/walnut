package com.site0.walnut.ext.data.thing.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cnqihbslVNHQ")
public class thing_tmpfile implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService ths = new WnThingService(sys, oTs);

        // 清理
        if (hc.params.has("clean")) {
            int limit = hc.params.getInt("clean", 1000);
            hc.output = ths.cleanTmpFile(limit);
        }
        // 默认就是创建咯
        else {
            String fnm = hc.params.val(0);
            String du = hc.params.get("expi", "1d");
            hc.output = ths.createTmpFile(fnm, du);
        }
    }

}
