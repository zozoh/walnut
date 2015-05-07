package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

@IocBean
public class cmd_cp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        if (args.length != 2) {
            throw Err.create("e.cmds.cp.not_enugh_args");
        }
        String ph_src = Wn.normalizeFullPath(args[0], sys);
        String ph_dst = Wn.normalizeFullPath(args[1], sys);
        WnObj src = sys.io.check(null, ph_src);
        WnObj dst = sys.io.createIfNoExists(null, ph_dst, src.race());

        if (!src.isFILE())
            throw Err.create("e.cmds.cp.only_file");

        InputStream ins = sys.io.getInputStream(src, 0);
        OutputStream ops = sys.io.getOutputStream(dst, 0);
        Streams.writeAndClose(ops, ins);
    }

}
