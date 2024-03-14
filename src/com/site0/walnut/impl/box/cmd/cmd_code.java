package com.site0.walnut.impl.box.cmd;

import java.io.Writer;

import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_code extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        String to = null;
        if (params.vals.length > 0) {
            to = params.vals[0];
        }

        if (Strings.isBlank(to))
            throw Er.create("e.cmd.code.nodstcode", to);

        // 得到源
        String str = null;
        if (sys.pipeId > 0) {
            str = sys.in.readAll();
        }

        if (!Strings.isBlank(str)) {
            Writer w = sys.out.getWriter(to);
            w.write(str);
        }

    }

}
