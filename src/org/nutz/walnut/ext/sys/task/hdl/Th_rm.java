package org.nutz.walnut.ext.sys.task.hdl;

import org.nutz.walnut.ext.sys.task.TaskCtx;
import org.nutz.walnut.ext.sys.task.TaskHdl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class Th_rm implements TaskHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        ZParams params = ZParams.parse(sc.args, "vr");

        // 准备要执行的命令
        StringBuilder sb = new StringBuilder("rm");

        boolean _v = params.is("v");
        boolean _r = params.is("r");

        if (_v || _r) {
            sb.append(" -");
            if (_v)
                sb.append("v");
            if (_r)
                sb.append("r");
        }

        // 生成要删除的 ID 列表
        // TODO zozoh: 要不要限制一下只能删任务呢？
        for (String tid : params.vals) {
            sb.append(" id:" + tid);
        }

        // 执行删除
        sys.exec(sb.toString());
    }

}
