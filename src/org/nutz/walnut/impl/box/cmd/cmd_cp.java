package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Each;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_cp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "p|v|r");
        if (params.vals.length != 2) {
            throw Err.create("e.cmds.cp.not_enugh_args");
        }
        String ph_src = Wn.normalizeFullPath(params.vals[0], sys);
        String ph_dst = Wn.normalizeFullPath(params.vals[1], sys);
        WnObj src = sys.io.check(null, ph_src);
        if (src.isDIR() && !params.is("r")) {
            throw Err.create("e.cmds.cp.omitting_directory");
        }

        _do_copy(sys, params, ph_src, ph_dst, src);
    }

    protected void _do_copy(final WnSystem sys,
                            final ZParams params,
                            final String base,
                            final String dst_base,
                            WnObj o) {
        // 打印
        if (params.is("v")) {
            sys.out.printlnf(Disks.getRelativePath(base, o.path()));
        }
        // 递归
        if (!o.isFILE() && params.is("r")) {
            sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    _do_copy(sys, params, base, dst_base, child);
                }
            });
        }
        // 删除自己
        String dstPath;
        if (base.equals(o.path())) {
            dstPath = dst_base;
        } else {
            dstPath = dst_base + o.path().substring(base.length());
        }
        WnObj dst = sys.io.createIfNoExists(null, dstPath, o.race());
        if (o.isFILE())
            __cp_src_as_file(sys, o, dst);
        if (params.is("p")) {
            NutMap meta = new NutMap();
            meta.put("mode", o.mode());
            meta.put("group", o.group());
            sys.io.appendMeta(dst, meta);
        }
    }

    private void __cp_src_as_file(WnSystem sys, WnObj src, WnObj dst) {

        // 执行快速 copy
        if (src.isMount())
            sys.io.writeAndClose(dst, sys.io.getInputStream(src, 0));
        else {
            sys.io.copyData(src, dst);
            WnContext wc = Wn.WC();
            wc.doHook("write", dst);
        }
    }

}
