package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_rm extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "rfv");

        // 参数错误
        if (params.vals.length < 1) {
            throw Er.create("e.cmd.mv.lackargs");
        }
        // 得到源
        List<WnObj> list = new LinkedList<WnObj>();
        WnObj p = evalCandidateObjs(sys, params.vals, list, false);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

        String base = p.path();

        // 循环删除
        for (WnObj o : list) {
            _do_delete(sys, params, base, o);
        }
    }

    protected void _do_delete(final WnSystem sys, final ZParams params, final String base, WnObj o) {
        // 打印
        if (params.is("v")) {
            sys.out.writeLinef(Disks.getRelativePath(base, o.path()));
        }
        // 递归
        if (!o.isFILE() && params.is("r")) {
            sys.io.eachChildren(o, null, new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    _do_delete(sys, params, base, child);
                }
            });
        }
        // 删除自己
        sys.io.delete(o);
    }

}
