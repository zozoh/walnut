package org.nutz.walnut.ext.entity.history;

import org.nutz.dao.Dao;
import org.nutz.lang.Mirror;
import org.nutz.lang.born.Borning;
import org.nutz.walnut.ext.entity.JvmDaoEntityExecutor;
import org.nutz.walnut.ext.sql.WnDaoConfig;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_history extends JvmDaoEntityExecutor {

    private static Borning<HistoryApi> born;

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        this.findHdlName(sys, hc, "history");
    }

    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (null == born) {
            Mirror<HistoryApi> mi = Mirror.me(HistoryApi.class);
            born = mi.getBorningByArgTypes(WnDaoConfig.class, Dao.class);
        }

        this.setupContext(sys, hc, born);
    }

}