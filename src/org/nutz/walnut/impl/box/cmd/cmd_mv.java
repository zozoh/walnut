package org.nutz.walnut.impl.box.cmd;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_mv extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        ZParams params = ZParams.parse(args, "v");

        // 参数错误
        if (params.vals.length < 2) {
            throw Er.create("e.cmd.mv.lackargs");
        }
        // 得到源
        String[] srcPaths = Arrays.copyOfRange(params.vals, 0, params.vals.length - 1);
        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, srcPaths, list, false);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        // 检查目标
        String dest = params.vals[params.vals.length - 1];
        String destPath = Wn.normalizeFullPath(dest, sys);
        WnObj oDest = sys.io.fetch(p, destPath);

        // 如果移动的是多个，那么目标必须是一个目录
        if (list.size() > 1) {
            if (null == oDest || oDest.isFILE()) {
                throw Er.create("e.cmd.mv.multidest.notdir", dest);
            }
        }

        // 逐个移动
        for (WnObj o : list) {
            String oldName = o.name();
            sys.io.move(o, destPath);
            // 显示
            if (params.is("v")) {
                // 换目录了
                if (dest.contains("/")) {
                    sys.out.writeLinef("%s -> %s/%s", oldName, dest, o.name());
                }
                // 仅仅是改名
                else {
                    sys.out.writeLinef("%s -> %s", oldName, o.name());
                }
            }
        }

    }
}
