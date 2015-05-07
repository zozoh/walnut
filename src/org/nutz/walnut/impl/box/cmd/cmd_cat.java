package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

@IocBean
public class cmd_cat extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        // 不能为空
        if (args.length == 0) {
            throw Er.create("e.cmd.noargs");
        }
        // 输出每个路径
        ZParams params = ZParams.parse(args, null);

        // 计算要列出的要处理的对象
        List<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, params.vals, list, false);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        // 没内容
        if (list.isEmpty()) {
            throw Er.create("e.io.obj.noexists", sys.original);
        }
        // 输出内容
        for (WnObj o : list) {
            // 目录不能输出
            if (o.isDIR()) {
                sys.err.writeLinef("e.io.readdir : %s", o.path());
            }
            // 输出内容
            else {
                String content = sys.io.readText(o);
                sys.out.write(content);
            }
        }
    }

}
