package org.nutz.walnut.ext.entity.newsfeed;

import org.nutz.dao.Dao;
import org.nutz.lang.Mirror;
import org.nutz.lang.born.Borning;
import org.nutz.walnut.ext.entity.JvmDaoEntityExecutor;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_newsfeed extends JvmDaoEntityExecutor {

    private static Borning<WnNewsfeedService> born;

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        this.findHdlName(sys, hc, "newsfeed");
    }

    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (null == born) {
            Mirror<WnNewsfeedService> mi = Mirror.me(WnNewsfeedService.class);
            born = mi.getBorningByArgTypes(NewsfeedConfig.class, Dao.class);
        }

        this.setupContext(sys, hc, NewsfeedConfig.class, born);
    }
}
