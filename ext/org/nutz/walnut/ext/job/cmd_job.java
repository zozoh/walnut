package org.nutz.walnut.ext.job;

import java.util.Arrays;

import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnJob;
import org.nutz.walnut.util.ZParams;

public class cmd_job extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
    	WnIo io = sys.io;
        if (args.length == 0)
            return;
        switch (args[0]) {
        case "start":
            if (!sys.se.me().equals("root")) {
                sys.err.println("only root can start job service");
                return;
            }
            if (!WnJob.me.isRunning()) {
            	WnJob.me.init();
            }
            else
                sys.out.println("aready started");
            return;
        case "stop":
            if (!sys.se.me().equals("root")) {
                sys.err.println("only root can stop job service");
                return;
            }
            if (!WnJob.me.isRunning()) {
                sys.out.println("job service isn't running");
                return;
            }
            WnJob.me.depose();
            return;
        case "add":
            if (!WnJob.me.isRunning()) {
                sys.err.println("job service isn't running");
                return;
            }
            ZParams params;
            if (args.length == 1) {
                params = ZParams.parse(new String[0], null);
            } else {
                params = ZParams.parse(Arrays.copyOfRange(args, 1, args.length), null);
            }
            String cmd;
            if (params.vals.length > 0)
                cmd = params.vals[0];
            else if (sys.pipeId > 0) {
                cmd = Streams.read(sys.in.getReader()).toString();
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
            metas.put("job_user", "root".equals(sys.me.name()) ? params.get("user", "root") : sys.me.name());
            metas.put("job_create_user", sys.me.name());
            metas.put("job_st", "wait");
            io.appendMeta(jobDir, metas);
            sys.out.print(id);
            return;
        case "status" :
            if (!WnJob.me.isRunning()) {
                sys.out.println("job service isn't running");
            } else {
                sys.out.printlnf("job service is running\n%s", WnJob.me);
            }
            return;
        default:
            break;
        }
    }
}
