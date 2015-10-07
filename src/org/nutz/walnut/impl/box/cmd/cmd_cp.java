package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

public class cmd_cp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        if (args.length != 2) {
            throw Err.create("e.cmds.cp.not_enugh_args");
        }
        String ph_src = Wn.normalizeFullPath(args[0], sys);
        String ph_dst = Wn.normalizeFullPath(args[1], sys);
        WnObj src = sys.io.check(null, ph_src);
        WnObj dst = sys.io.fetch(null, ph_dst);

        // Copy 单个文件
        if (src.isFILE()) {
            __cp_src_as_file(sys, src, dst);
        }
        // Copy 一组目录
        else {
            throw Er.create("e.cmd.cp.src.dir", "unsupported");
        }
    }

    private void __cp_src_as_file(WnSystem sys, WnObj src, WnObj dst) {
        WnObj dst_o;
        // 如果目标是个目录
        if (dst.isDIR()) {
            dst_o = sys.io.createIfNoExists(dst, src.name(), src.race());
        } else {
            dst_o = dst;
        }

        if (!src.isFILE())
            throw Err.create("e.cmds.cp.only_file");

        InputStream ins = sys.io.getInputStream(src, 0);
        OutputStream ops = sys.io.getOutputStream(dst_o, 0);
        Streams.writeAndClose(ops, ins);
    }

}
