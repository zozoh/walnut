package org.nutz.walnut.ext.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.quartz.Quartz;
import org.nutz.quartz.QzEach;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_job extends JvmExecutor implements Callable<Object> {

    protected ThreadPoolExecutor es;
    protected String root = "/sys/job";
    protected String tmpRoot = "/sys/job/tmp";
    protected WnIo io;
    protected WnSystem _sys;

    public boolean isRunning() {
        return es != null && !es.isShutdown();
    }

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length == 0)
            return;
        switch (args[0]) {
        case "start":
            if (!sys.se.me().equals("root")) {
                sys.err.println("only root can start job service");
                return;
            }
            if (!isRunning()) {
                init(sys);
            }
            else
                sys.out.println("aready started");
            return;
        case "stop":
            if (!sys.se.me().equals("root")) {
                sys.err.println("only root can stop job service");
                return;
            }
            if (!isRunning()) {
                sys.out.println("job service isn't running");
                return;
            }
            es.shutdown();
            return;
        case "add":
            if (!isRunning()) {
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
            WnObj jobDir = io.create(null, root + "/" + id, WnRace.DIR);
            WnObj cmdFile = io.create(jobDir, "cmd", WnRace.FILE);
            io.writeText(cmdFile, cmd);
            NutMap metas = new NutMap();
            metas.put("job_name", name);
            metas.put("job_cron", cron);
            metas.put("job_ava", System.currentTimeMillis());
            metas.put("job_st", "wait");
            io.appendMeta(jobDir, metas);
            sys.out.print(id);
            return;
        case "status" :
            if (!isRunning()) {
                sys.out.println("job service isn't running");
            } else {
                sys.out.printlnf("job service is running, %d active", es.getActiveCount());
            }
            return;
        default:
            break;
        }
    }

    public void init(WnSystem sys) {
        if (es == null)
            es = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);
        this.io = sys.io;
        this._sys = sys;
        io.createIfNoExists(null, root, WnRace.DIR);
        io.createIfNoExists(null, tmpRoot, WnRace.DIR);
        es.submit(this);
    }

    public WnObj next() {
        Date now = new Date();
        String pid = io.check(null, root).id();
        WnQuery query = new WnQuery();
        query.setv("pid", pid);
        //query.limit(1);
        //query.sortBy("job_ava", 1);
        query.setv("job_ava", new NutMap().setv("$lt", now.getTime()));
        List<WnObj> list = io.query(query);
        if (list == null || list.isEmpty())
            return null;
        WnObj jobDir = list.get(0);
        String cron = jobDir.getString("job_cron");
        if (Strings.isBlank(cron)) {
            io.appendMeta(jobDir, "job_ava:"+(now.getTime()+24*60*60*1000L));
            return jobDir;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        if (calendar.get(Calendar.HOUR_OF_DAY) == 23 && calendar.get(Calendar.MINUTE) > 55) {
            calendar.set(Calendar.MINUTE, 56);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            io.appendMeta(jobDir, "job_ava:"+calendar.getTimeInMillis());
        } else {
            calendar.set(Calendar.MINUTE, 56);
            io.appendMeta(jobDir, "job_ava:"+calendar.getTimeInMillis());
        }
        // TODO 如果cron是非法的,就挂了
        Quartz quartz = Quartz.NEW(cron);
        if (!quartz.matchDate(calendar)) {
            return null;
        }
        int MIN_TNT = 3; // 3分钟间隔
        Integer[] jobs = new Integer[24*60/MIN_TNT];
        quartz.each(jobs, new QzEach<Integer>() {
            public void invoke(Integer[] array, int index) throws Exception {
                calendar.set(Calendar.HOUR_OF_DAY, (index*MIN_TNT)/60);
                calendar.set(Calendar.MINUTE, (index*MIN_TNT)%60);
                WnObj _jobDir = io.create(null, tmpRoot+"/"+R.UU32(), WnRace.DIR);
                WnObj _jobCmd = io.create(_jobDir, "cmd", WnRace.FILE);
                // TODO 咋拷贝文件夹呢?
            }
        }, calendar);
        return jobDir;
    }

    public Object call() throws Exception {
        while (true) {
            WnObj next = this.next();
            if (next == null) {
                Lang.quiteSleep(1000);
                continue;
            }
            es.submit(new WalnutJobExecutor(next));
        }
    }

    public class WalnutJobExecutor implements Callable<Object> {

        protected WnObj jobDir;

        public WalnutJobExecutor(WnObj job) {
            this.jobDir = job;
        }

        public Object call() throws Exception {
            try {
                io.appendMeta(jobDir, new NutMap().setv("job_start", System.currentTimeMillis()).setv("job_st", "run"));
                WnObj cmdFile = io.fetch(jobDir, "cmd");
                if (cmdFile != null) {
                    String cmd = io.readText(cmdFile);
                    _sys.exec(cmd, NopOut, NopOut, NopIn);
                }
            } finally {
                io.appendMeta(jobDir, new NutMap().setv("job_end", System.currentTimeMillis()).setv("job_st", "done"));
            }
            return null;
        }

    }
    static NopOutputStream NopOut = new NopOutputStream();
    static NopInputStream NopIn = new NopInputStream();
    static class NopOutputStream extends OutputStream {
        public void write(int b) throws IOException {}
    }
    static class NopInputStream extends InputStream {
        public int read() throws IOException {
            return -1;
        }
    }
}
