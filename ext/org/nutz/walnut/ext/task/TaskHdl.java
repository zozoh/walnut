package org.nutz.walnut.ext.task;

import org.nutz.walnut.impl.box.WnSystem;

public interface TaskHdl {

    void invoke(WnSystem sys, TaskCtx sc) throws Exception;

}
