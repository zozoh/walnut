package org.nutz.walnut.ext.task.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.task.TaskCtx;
import org.nutz.walnut.ext.task.TaskHierarchy;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_after extends AbstractTaskHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        if (null == sc.oTask)
            throw Er.create("e.cmd.task.notask");

        ZParams params = ZParams.parse(sc.args, "v");

        if (params.vals.length == 0)
            throw Er.create("e.cmd.task.after.noafter");

        // 要移动到某个新的 ID
        String taId = params.vals[0];

        // 自己的前后关系
        TaskHierarchy thi = this._hierarchy_gen(sys, sc.oTask);

        // 如果更换了新的
        if (!Lang.equals(taId, thi.prevId)) {
            // 当前的前后关系
            WnObj A = Strings.isBlank(thi.prevId) ? null : sys.io.checkById(thi.prevId);
            WnObj B = sc.oTask;
            WnObj C = Strings.isBlank(thi.nextId) ? null : sys.io.checkById(thi.nextId);

            // 目标的前后关系
            WnObj Y = sys.io.checkById(taId);
            TaskHierarchy thi2 = this._hierarchy_gen(sys, Y);
            WnObj Z = Strings.isBlank(thi2.nextId) ? null : sys.io.checkById(thi2.nextId);

            // 修改新的父
            if (!Lang.equals(B.parentId(), Y.parentId())) {
                B = sys.io.move(B, Y.parent().path());
                sc.oTask = B;
            }

            // 修改关系
            __set(A, "next", C);
            __set(C, "prev", A);
            __set(B, "prev", Y);
            __set(B, "next", Z);
            __set(Y, "next", B);
            __set(Z, "prev", B);

            // 持久化
            __save(sys, A, "^next$");
            __save(sys, C, "^prev$");
            __save(sys, B, "^prev|next$");
            __save(sys, Y, "^next$");
            __save(sys, Z, "^prev$");
        }

        // 完毕
        this._done(sys, params, sc.oTask);
    }

    private void __save(WnSystem sys, WnObj o, String regex) {
        if (null != o)
            sys.io.appendMeta(o, regex);
    }

    private void __set(WnObj o, String key, WnObj ta) {
        if (null != o) {
            o.setv(key, null == ta ? null : ta.id());
        }
    }

}
