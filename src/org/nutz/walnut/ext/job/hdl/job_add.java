package org.nutz.walnut.ext.job.hdl;

import java.io.IOException;

import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.repo.Base64;
import org.nutz.walnut.ext.job.WnJobService;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs("Q")
public class job_add extends job_abstract {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!WnJobService.me.isRunning()) {
            sys.err.println("job service isn't running");
            return;
        }
        ZParams params;
        if (hc.args.length == 0) {
            params = ZParams.parse(new String[0], null);
        } else {
            params = ZParams.parse(hc.args, null);
        }
        String cmd;
        if (params.vals.length > 0) {
            cmd = params.vals[0];
            if ("true".equals(params.get("base64")))
                cmd = new String(Base64.decode(cmd), Encoding.CHARSET_UTF8);
        } else if (sys.pipeId > 0) {
            try {
                cmd = Streams.read(sys.in.getReader()).toString();
            }
            catch (IOException e) {
                throw Err.create("e.cmds.job.ioerr", e);
            }
        } else {
            sys.err.print("need cmd");
            return;
        }
        if (Strings.isBlank(cmd)) {
            sys.err.println("cmd is blank");
            return;
        }
        String cron = params.get("cron");
        String name = params.get("name");
        String[] id = new String[1];

        // 用根用户权限执行
        String cmdText = cmd;
        String runByUser ="root".equals(sys.me.name()) ? hc.params.get("user", "root") : sys.me.name();
        WnRun.sudo(sys, () -> {
            id[0] = WnJobService.me.addJob(cmdText, name, cron, sys.me.name(), runByUser, sys.se.vars());
        });

        // 打印任务的 ID
        if (!params.is("Q"))
            sys.out.print(id[0]);

        return;
    }

}
