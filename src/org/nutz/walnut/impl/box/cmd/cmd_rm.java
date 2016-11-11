package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_rm extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "rfv");
        boolean isV = params.is("v");
        boolean isR = params.is("r");

        // 参数错误
        if (params.vals.length < 1) {
            throw Er.create("e.cmd.mv.lackargs");
        }

        // 得到当前的目录
        WnObj oCurrent = this.getCurrentObj(sys);
        String base = oCurrent.path();

        // 循环每个参数
        WnQuery q = Wn.Q.pid(oCurrent);
        for (String str : params.vals) {
            // 修改通配符
            str = str.replace("*", ".*");

            // 设置查询条件
            q.setv("nm", str);

            // 挨个查一下，然后删除
            sys.io.each(q, new Each<WnObj>() {
                public void invoke(int index, WnObj o, int length) {
                    _do_delete(sys, isV, isR, base, o);
                }
            });
        }

    }

    protected void _do_delete(final WnSystem sys,
                              final boolean isV,
                              final boolean isR,
                              final String base,
                              WnObj o) {
        // 打印
        if (isV) {
            sys.out.printlnf(Disks.getRelativePath(base, o.path()));
        }

        // 递归
        if (!o.isFILE() && isR) {
            sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    _do_delete(sys, isV, isR, base, child);
                }
            });
        }
        // 删除自己
        sys.io.delete(o);
    }

}
