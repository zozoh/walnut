package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_ln extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "Ffhinsv");

        if (params.vals.length != 2) {
            throw Er.create("e.cmd.invalidargs", args);
        }

        String srcPath = params.vals[0];
        String src = Wn.normalizeFullPath(srcPath, sys.session);
        String dst = Wn.normalizePath(params.vals[1], sys.session);

        // 确保源存在
        WnObj oSrc = sys.io.check(null, src);

        WnObj p = sys.getCurrentObj();

        // 查看目标
        WnObj oDst = sys.io.fetch(p, dst);

        // 预先处理
        if (null != oDst) {
            if (params.is("f")) {
                sys.io.delete(oDst);
                oDst = null;
            } else {
                throw Er.create("e.cmd.ln.exists", dst);
            }
        }

        // 创建链接
        oDst = sys.io.create(p, dst, oSrc.race());

        // 要链接的内容
        String ln;
        if (params.is("s")) {
            // 绝对
            if (srcPath.startsWith("/") || srcPath.startsWith("~")) {
                ln = oSrc.path();
            }
            // 相对
            else {
                ln = Disks.getRelativePath(oDst.path(), oSrc.path());
            }
        } else {
            ln = "id:" + oSrc.id();
        }

        // 设置链接
        oDst.link(ln);
        sys.io.set(oDst, "^ln$");
    }

}
