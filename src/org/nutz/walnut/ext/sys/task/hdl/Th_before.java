package org.nutz.walnut.ext.sys.task.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.task.TaskCtx;
import org.nutz.walnut.ext.sys.task.TaskHierarchy;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_before extends AbstractOrderHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        if (null == sc.oTask)
            throw Er.create("e.cmd.task.notask");

        ZParams params = ZParams.parse(sc.args, "v");

        if (params.vals.length == 0)
            throw Er.create("e.cmd.task.before.nobefore");

        // 要移动到某个新的 ID
        String taId = params.vals[0];

        // 自己的前后关系
        TaskHierarchy thi = this._hierarchy_gen(sys, sc.oTask);

        // 如果更换了新的
        if (!Lang.equals(taId, thi.prevId)) {
            // 目标的前后关系
            WnObj Y = sys.io.checkById(taId);
            TaskHierarchy thi2 = this._hierarchy_gen(sys, Y);
            WnObj X = Strings.isBlank(thi2.prevId) ? null : sys.io.checkById(thi2.prevId);

            // 当前的前后关系
            WnObj A = Strings.isBlank(thi.prevId) ? null : sys.io.checkById(thi.prevId);
            WnObj B = sc.oTask;
            WnObj C = Strings.isBlank(thi.nextId) ? null : sys.io.checkById(thi.nextId);

            // 修改新的父
            if (!Lang.equals(B.parentId(), Y.parentId())) {
                B = sys.io.move(B, Y.parent().path());
                sc.oTask = B;
            }

            // 修改关系
            _set(A, "next", B, "next");
            _set(C, "prev", B, "prev");
            _set(B, "prev", Y, "prev");
            _set(B, "next", Y, null);
            _set(X, "next", B, null);
            _set(Y, "prev", B, null);

            // 合并修改
            Map<String, OrderUpdateInfo> map = new HashMap<String, OrderUpdateInfo>();
            this._join_obj(map, A, "next");
            this._join_obj(map, C, "prev");
            this._join_obj(map, B, "prev", "next");
            this._join_obj(map, X, "next");
            this._join_obj(map, Y, "prev");

            // 持久化
            this._save(sys, map);

        }

        // 完毕
        this._done(sys, params, sc.oTask);
    }

}
