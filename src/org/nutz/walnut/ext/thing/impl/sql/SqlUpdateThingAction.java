package org.nutz.walnut.ext.thing.impl.sql;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.entity.Record;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.UpdateThingAction;

public class SqlUpdateThingAction extends UpdateThingAction {

    @Override
    public WnObj invoke() {
        SqlThingContext ctx = SqlThingMaster.me().getSqlThingContext(oTs);
        ctx.dao.update(ctx.table, Chain.from(meta), Cnd.where("id", "=", id));
        Record re = ctx.dao.fetch(ctx.table, Cnd.where("id", "=", id));
        if (re == null)
            return null;
        return SqlThingMaster.asWnObj(oTs, checkDirTsIndex(), re.sensitive());
    }

}
