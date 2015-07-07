package org.nutz.walnut.ext.task.hdl;

import java.util.TimeZone;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.task.TaskCtx;
import org.nutz.walnut.ext.task.TaskStatus;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_add extends AbstractTaskHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        ZParams params = ZParams.parse(sc.args, "v");
        if (params.vals.length == 0)
            throw Er.create("e.cmd.task.add.notitle");

        String title = params.vals[0];
        String tid = params.get("id");
        WnObj oParent = null;
        WnObj oPrev = null;
        WnObj oNext = null;

        // 得到新任务的父对象
        // 如果指定了 prev
        if (params.has("prev")) {
            oPrev = sys.io.checkById(params.check("prev"));
            oParent = sys.io.getParent(oPrev);
            String nextId = oPrev.getString("next");
            if (!Strings.isBlank(nextId)) {
                oNext = sys.io.checkById(nextId);
            }
        }
        // 如果指定了 next
        else if (params.has("next")) {
            oNext = sys.io.checkById(params.check("next"));
            oParent = sys.io.getParent(oNext);
            String prevId = oNext.getString("prev");
            if (!Strings.isBlank(prevId)) {
                oPrev = sys.io.checkById(prevId);
            }
        }
        // 如果指定了父任务，那么就会插入的任务的结尾
        else if (null != sc.oTask) {
            oParent = sc.oTask;
            WnQuery q = new WnQuery();
            q.setv("pid", oParent.id()).setv("next", null);
            oPrev = sys.io.getOne(q);
        }
        // 那么必须是根任务了
        else {
            oParent = sys.io.createIfNoExists(sc.oHome, "mine", WnRace.DIR);
        }

        // 创建新的任务对象
        WnObj oNew = sys.io.createById(oParent, tid, "${id}", WnRace.DIR);

        // 修改前后任务的链接关系
        if (null != oPrev) {
            oPrev.setv("next", oNew.id());
            oNew.setv("prev", oPrev.id());
            sys.io.appendMeta(oPrev, "^next$");
        }

        if (null != oNext) {
            oNext.setv("prev", oNew.id());
            oNew.setv("next", oNext.id());
            sys.io.appendMeta(oNext, "^prev$");
        }

        // 修改任务的其他元数据，并更新
        oNew.setv("title", title);
        if (params.has("lbls")) {
            String[] lbls = Strings.splitIgnoreBlank(params.get("lbls"));
            oNew.labels(lbls);
        }
        if(params.has("ow")){
            oNew.setv("ow", params.get("ow"));
        }
        oNew.setv("status", TaskStatus.NEW);
        oNew.setv("tzone", TimeZone.getDefault().getID());
        oNew.type("task");
        sys.io.appendMeta(oNew, "^ow|tp|lbls|title|tzone|status|prev|next$");

        // 完毕
        this._done(sys, params, oNew);
    }

}
