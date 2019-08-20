package org.nutz.walnut.ext.thing.impl.sql;

import java.util.LinkedList;
import java.util.List;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.entity.Record;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.DeleteThingAction;
import org.nutz.walnut.ext.thing.util.Things;

public class SqlDeleteThingAction extends DeleteThingAction {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<WnObj> invoke() {
        // 数据目录的主目录
        WnObj oData = this.checkDirTsData();

        // 准备返回结果
        List<NutMap> output = new LinkedList<>();
        SqlThingContext ctx = SqlThingMaster.me().getSqlThingContext(oTs);

        for (String id : ids) {
            // 得到对应对 Thing
            Record re = ctx.dao.fetch(ctx.table, Cnd.where("id", "=", id));
            if (re == null)
                continue;

            // 硬删除，或者已经是删除的了，那么真实的删除数据对象
            if (this.hard || re.getInt("th_live", 0) == Things.TH_DEAD) {
                // 删除数据对象
                WnObj oThData = io.fetch(oData, id);
                if (null != oThData) {
                    io.delete(oThData, true);
                }
                // 删除索引
                ctx.dao.clear(ctx.table, Cnd.where("id", "=", id));
            }
            // 标记为删除
            else {
                ctx.dao.update(ctx.table, Chain.make("th_live", 0), Cnd.where("id", "=", id));
                NutMap tmp = new NutMap(re.sensitive());
                tmp.put("th_live", false);
                output.add(tmp);
            }
        }

        // 返回输出
        return SqlThingMaster.asWnObj(oTs, this.checkDirTsIndex(), (List) output);

    }

}
