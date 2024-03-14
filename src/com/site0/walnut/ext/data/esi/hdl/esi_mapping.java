package com.site0.walnut.ext.data.esi.hdl;

import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.data.esi.EsiConf;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

// https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html
public class esi_mapping extends esi_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        EsiConf conf = conf(sys, hc);
        if (conf == null) {
            sys.err.print("e.cmd.esi.mapping.miss_esi_conf");
            return;
        }
        esi(hc.ioc).putMapping(sys.getMyName(), conf);
        sys.out.writeJson(conf, JsonFormat.full());
    }

}
