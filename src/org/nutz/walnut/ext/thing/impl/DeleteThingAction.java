package org.nutz.walnut.ext.thing.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;

public class DeleteThingAction extends ThingAction<List<WnObj>> {

    private Collection<String> ids;

    private boolean quiet;

    public DeleteThingAction setIds(Collection<String> ids) {
        this.ids = ids;
        return this;
    }

    public DeleteThingAction setIds(String... ids) {
        this.ids = Lang.list(ids);
        return this;
    }

    public DeleteThingAction setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    @Override
    public List<WnObj> invoke() {

        output = new LinkedList<>();

        for (String id : ids) {
            // 得到对应对 Thing
            WnObj oT = this.checkThIndex(id);

            // 已经是删除的了
            if (oT.getInt("th_live", 0) == Things.TH_DEAD) {
                if (!this.quiet) {
                    throw Er.create("e.cmd.thing.delete.already", oT.id());
                }
            }
            // 执行删除
            else {
                oT.setv("th_live", Things.TH_DEAD);
                io.set(oT, "^th_live$");
                output.add(oT);
            }
        }

        // 返回输出
        return output;

    }

}
