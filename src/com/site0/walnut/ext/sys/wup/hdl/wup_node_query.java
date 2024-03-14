package com.site0.walnut.ext.sys.wup.hdl;

import org.nutz.json.JsonFormat;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

/**
 * 节点配置列表
 * @author wendal
 *
 */
public class wup_node_query implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // TODO 支持分页和查询语句
        String confsDir = Wn.normalizeFullPath("~/wup/confs/", sys);
        WnObj confs = sys.io.createIfNoExists(null, confsDir, WnRace.DIR);
        WnQuery query = Wn.Q.pid(confs).sortBy("nm", 1);
        sys.out.writeJson(sys.io.query(query), JsonFormat.full());
    }

}
