package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.util.List;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

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
        List<WnObj> list = evalCandidateObjsNoEmpty(sys, params.vals, false);

        // 没内容
        if (list.isEmpty()) {
            throw Er.create("e.io.obj.noexists", sys.original);
        }
        // 输出内容
        for (WnObj o : list) {
            // 目录不能输出
            if (o.isDIR()) {
                sys.err.printlnf("e.io.readdir : %s", o.path());
                continue;
            }
            // 输出内容
            InputStream ins = sys.io.getInputStream(o, 0);
            try {
                sys.out.write(ins);
            }
            finally {
                Streams.safeClose(ins);
            }
        }
    }

}
