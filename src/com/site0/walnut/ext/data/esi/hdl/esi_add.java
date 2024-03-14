package com.site0.walnut.ext.data.esi.hdl;

import org.nutz.lang.Lang;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.esi.EsiConf;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class esi_add extends esi_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        EsiConf conf = conf(sys, hc);
        if (conf == null) {
            sys.err.print("e.cmd.esi.mapping.miss_esi_conf");
            return;
        }
        if (hc.params.vals.length > 0) {
            // 添加路径, 支持批量
            for (String tmp : hc.params.vals) {
                String path = Wn.normalizeFullPath(tmp, sys);
                WnObj wobj = sys.io.check(null, path);
                esi(hc.ioc).addOrUpdateMeta(conf, wobj);
            }
        }
        else {
            // 指定id和数据, 直接添加
            String id = hc.params.check("id");
            String data = hc.params.check("data");
            esi(hc.ioc).addOrUpdateMeta(sys.getMyName(), conf, id, Lang.map(data));
        }
    }

}
