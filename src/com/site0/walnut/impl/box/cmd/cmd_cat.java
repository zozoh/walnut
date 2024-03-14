package com.site0.walnut.impl.box.cmd;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_cat extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        // 不能为空
        if (args.length == 0) {
            throw Er.create("e.cmd.noargs");
        }
        // 输出每个路径
        ZParams params = ZParams.parse(args, "^(quiet)$");
        boolean isQuiet = params.is("quiet");

        // 计算要列出的要处理的对象
        List<WnObj> list = null;

        // 尝试获取
        try {
            list = Cmds.evalCandidateObjsNoEmpty(sys, params.vals, 0);
        }
        catch (Throwable e) {
            // 静默模式
            if (isQuiet) {
                list = new LinkedList<>();
            }
            // 普通模式，抛错
            else {
                throw Er.wrap(e);
            }
        }

        // 没内容
        if (list.isEmpty()) {
            if (!isQuiet) {
                throw Er.create("e.io.obj.noexists", sys.cmdOriginal);
            }
        }
        // 输出内容
        for (WnObj o : list) {
            // 解开链接
            WnObj o2 = Wn.real(o, sys.io, new HashMap<>());
            // 目录不能输出
            if (o2.isDIR()) {
                sys.err.printlnf("e.io.readdir : %s", o2.path());
                continue;
            }
            // 输出内容
            InputStream ins = sys.io.getInputStream(o2, 0);
            try {
                sys.out.write(ins);
            }
            finally {
                Streams.safeClose(ins);
            }
        }
    }

}
