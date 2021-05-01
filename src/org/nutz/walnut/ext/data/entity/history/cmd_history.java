package org.nutz.walnut.ext.data.entity.history;

import org.nutz.dao.Dao;
import org.nutz.lang.Mirror;
import org.nutz.lang.born.Borning;
import org.nutz.walnut.ext.data.entity.JvmDaoEntityExecutor;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_history extends JvmDaoEntityExecutor {

    private static Borning<? extends HistoryApi> born;

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        this.findHdlName(sys, hc, "history");
    }

    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (null == born) {
            Mirror<WnHistoryService> mi = Mirror.me(WnHistoryService.class);
            born = mi.getBorningByArgTypes(HistoryConfig.class, Dao.class);
        }

        this.setupContext(sys, hc, HistoryConfig.class, born);
    }

}
