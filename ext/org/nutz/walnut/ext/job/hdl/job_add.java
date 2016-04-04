package org.nutz.walnut.ext.job.hdl;

import java.io.IOException;

import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJob;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

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
        if (params.vals.length > 0)
            cmd = params.vals[0];
        else if (sys.pipeId > 0) {
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
        WnObj jobDir = io.create(null, WnJob.root + "/" + id, WnRace.DIR);
        WnObj cmdFile = io.create(jobDir, "cmd", WnRace.FILE);
        io.writeText(cmdFile, cmd);
        NutMap metas = new NutMap();
        metas.put("job_name", name);
        metas.put("job_cron", cron);
        metas.put("job_ava", System.currentTimeMillis());
        metas.put("job_st", "wait");
        metas.put("job_user",
                  "root".equals(sys.me.name()) ? params.get("user", "root") : sys.me.name());
        metas.put("job_create_user", sys.me.name());
        metas.put("job_st", "wait");
        io.appendMeta(jobDir, metas);
        sys.out.print(id);
        return;
    }

}
