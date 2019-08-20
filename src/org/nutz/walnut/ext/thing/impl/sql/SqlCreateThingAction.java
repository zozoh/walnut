package org.nutz.walnut.ext.thing.impl.sql;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.CreateThingAction;

public class SqlCreateThingAction extends CreateThingAction {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<WnObj> invoke() {
        SqlThingContext ctx = SqlThingMaster.me().getSqlThingContext(this.oTs);
        long timenow = System.currentTimeMillis();
        for (NutMap meta : metas) {
            // 补齐id/ct/lm属性
            if (Strings.isBlank(meta.getString("id"))) {
                meta.put("id", R.UU32());
            }
            meta.put("ct", timenow);
            meta.put("lm", timenow);
            // 移除肯定不支持的属性
            meta.remove("race");
            meta.remove("pid");
            meta.remove("d0");
            meta.remove("d1");
            meta.remove("c");
            meta.remove("m");
            meta.remove("g");
            meta.remove("md");
            meta.remove("ph");
        }
        metas.get(0).put(".table", ctx.table);
        ctx.dao.fastInsert(metas);
        return SqlThingMaster.asWnObj(oTs, this.checkDirTsIndex(), (List) metas);
    }
}
