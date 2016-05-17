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
        
        WnObj oSrc = sys.io.check(null, ph_src);
        if (oSrc.isDIR() && !params.is("r")) {
            throw Err.create("e.cmds.cp.omitting_directory");
        }

        _do_copy(sys, params, ph_src, ph_dst, oSrc);
    }

    protected void _do_copy(final WnSystem sys,
                            final ZParams params,
                            final String ph_src,
                            final String ph_dst,
                            WnObj oSrc) {
        // 打印
        if (params.is("v")) {
            sys.out.printlnf(Disks.getRelativePath(ph_src, oSrc.path()));
        }
        // 递归
        if (!oSrc.isFILE() && params.is("r")) {
            sys.io.each(Wn.Q.pid(oSrc.id()), new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    _do_copy(sys, params, ph_src, ph_dst, child);
                }
            });
        }
        // 删除自己
        String dstPath;
        if (ph_src.equals(oSrc.path())) {
            dstPath = ph_dst;
        } else {
            dstPath = ph_dst + oSrc.path().substring(ph_src.length());
        }
        WnObj dst = sys.io.createIfNoExists(null, dstPath, oSrc.race());

        if (oSrc.isFILE())
            __cp_src_as_file(sys, oSrc, dst);

        if (params.is("p")) {
            NutMap meta = new NutMap();
            meta.put("mode", oSrc.mode());
            meta.put("group", oSrc.group());
            meta.put("tp", oSrc.type());
            meta.put("mime", oSrc.mime());
            sys.io.appendMeta(dst, meta);
        }
    }

    private void __cp_src_as_file(WnSystem sys, WnObj src, WnObj dst) {

        // 如果是 Mount 就傻傻的写流
        if (src.isMount()) {
            sys.io.writeAndClose(dst, sys.io.getInputStream(src, 0));
        }
        // 执行快速 copy
        else {
            sys.io.copyData(src, dst);
            WnContext wc = Wn.WC();
            wc.doHook("write", dst);
        }
    }

}
