package org.nutz.walnut.ext.thing.impl.sql;

import org.nutz.dao.Cnd;
import org.nutz.dao.entity.Record;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.GetThingAction;

public class SqlGetThingAction extends GetThingAction {

    public WnObj getThIndex(String id) {
        SqlThingContext ctx = SqlThingMaster.me().getSqlThingContext(oTs);
        Record re = ctx.dao.fetch(ctx.table, Cnd.where("id", "=", id));
        if (re == null)
            return null;
        return SqlThingMaster.asWnObj(oTs, oTs, re.sensitive());
    }
}
