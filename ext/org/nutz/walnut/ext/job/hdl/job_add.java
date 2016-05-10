package org.nutz.walnut.ext.job.hdl;

import java.io.IOException;

import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.repo.Base64;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.job.WnJob;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs("Q")
public class job_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!WnJob.me.isRunning()) {
            sys.err.println("job service isn't running");
            return;
        }
        WnIo io = sys.io;
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
        String id = R.UU32();

        // 用根用户权限执行
        final String cmdText = cmd;
        WnContext wc = Wn.WC();
        wc.security(new WnEvalLink(sys.io), () -> {
            WnUsr root = sys.usrService.check("root");
            wc.su(root, () -> {
                WnObj jobDir = io.create(null, WnJob.root + "/" + id, WnRace.DIR);
                WnObj cmdFile = io.create(jobDir, "cmd", WnRace.FILE);
                io.writeText(cmdFile, cmdText);
                NutMap metas = new NutMap();
                metas.put("job_name", name);
                metas.put("job_cron", cron);
                metas.put("job_ava", System.currentTimeMillis());
                metas.put("job_st", "wait");
                metas.put("job_user",
                          "root".equals(sys.me.name()) ? params.get("user", "root")
                                                       : sys.me.name());
                metas.put("job_create_user", sys.me.name());
                metas.put("job_st", "wait");
                metas.put("job_env", sys.se.vars());
                io.appendMeta(jobDir, metas);
            });
        });

        // 打印任务的 ID
        if (!params.is("Q"))
            sys.out.print(id);

        return;
    }

}
