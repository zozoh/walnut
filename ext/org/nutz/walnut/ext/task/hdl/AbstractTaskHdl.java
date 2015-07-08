package org.nutz.walnut.ext.task.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.task.TaskHdl;
import org.nutz.walnut.ext.task.TaskHierarchy;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public abstract class AbstractTaskHdl implements TaskHdl {

    protected TaskHierarchy _hierarchy_gen(WnSystem sys, WnObj oTask) {
        TaskHierarchy thi = new TaskHierarchy();

        thi.parentId = oTask.parentId();
        thi.prevId = oTask.getString("prev");
        thi.nextId = oTask.getString("next");

        return thi;
    }

    protected void _done(WnSystem sys, ZParams params, WnObj obj) {
        if (params.is("v")) {
            sys.out.println(Json.toJson(obj));
        }
    }

}
