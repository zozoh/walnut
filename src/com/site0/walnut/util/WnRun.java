package com.site0.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.web.WnConfig;

@IocBean
public class WnRun {

    private static final Log boxLog = Wlog.getBOX();

    @Inject("refer:serviceFactory")
    private WnServiceFactory services;

    @Inject("refer:io")
    private WnIo io;

    @Inject("refer:sysTaskService")
    private WnSysTaskApi taskApi;

    @Inject("refer:sysCronService")
    private WnSysCronApi cronApi;

    @Inject("refer:sysScheduleService")
    private WnSysScheduleApi scheduleApi;

    @Inject("refer:sysLoginService")
    private WnLoginApi login;

    // @Inject("refer:sysAuthService")
    // private WnAuthService auth;

    @Inject("refer:jvmExecutorFactory")
    private JvmExecutorFactory jef;

    @Inject("refer:boxService")
    private WnBoxService boxes;

    @Inject("refer:hookService")
    private WnHookService hooks;

    @Inject("java:$conf.getInt('box-alloc-timeout')")
    protected int allocTimeout;

    @Inject("refer:conf")
    protected WnConfig conf;

    public WnServiceFactory getServiceFactory() {
        return services;
    }

    public WnIo io() {
        return io;
    }

    public WnLoginApi login() {
        return this.login;
    }

    public WnBoxService boxes() {
        return boxes;
    }

    public WnHookService hooks() {
        return hooks;
    }

    public JvmExecutorFactory jef() {
        return jef;
    }

    public String exec(String logPrefix, String unm, final String cmdText) {
        return exec(logPrefix, unm, null, cmdText);
    }

    public String exec(String logPrefix, String unm, String input, String cmdText) {
        // 检查用户和会话
        WnUser u = login.checkUser(unm);
        long du = login.getSessionDuration(false);
        final WnSession se = login.createSession(u, du);

        // 执行命令
        try {
            return exec(logPrefix, se, input, cmdText);
        }
        // 退出会话
        finally {
            login.removeSession(se);
        }
    }

    public void exec(String logPrefix,
                     String unm,
                     String input,
                     String cmdText,
                     StringBuilder sbOut,
                     StringBuilder sbErr) {
        // 检查用户和会话
        final WnSession se = creatSession(unm, false);
        InputStream in = null == input ? null : Wlang.ins(input);
        OutputStream out = Wlang.ops(sbOut);
        OutputStream err = Wlang.ops(sbErr);
        // 执行命令
        try {
            this.exec(logPrefix, se, cmdText, out, err, in, null);
        }
        // 退出会话
        finally {
            login.removeSession(se);
        }
    }

    public WnSession creatSession(String unm, boolean longSession) {
        WnUser u = login.checkUser(unm);

        // zozoh: 为啥？考，应该直接创建就好了吧 ...
        // return Wn.WC().su(u, new Proton<WnSession>() {
        // protected WnSession exec() {
        // return sess.create(u);
        // }
        // });

        return auth.createSession(u, longSession);
    }

    public String exec(String logPrefix, WnAuthSession se, String cmdText) {
        return exec(logPrefix, se, null, cmdText);
    }

    public String exec(String logPrefix, WnAuthSession se, String input, String cmdText) {
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        OutputStream out = Wlang.ops(sbOut);
        OutputStream err = Wlang.ops(sbErr);
        InputStream in = null == input ? null : Wlang.ins(input);

        exec(logPrefix, se, cmdText, out, err, in, null);

        if (sbErr.length() > 0)
            throw Er.create("e.cmd.error", sbErr);

        return sbOut.toString();
    }

    public WnBoxRunning createRunning(boolean withHook) {
        return createRunning(null, withHook, null, null, null);
    }

    public WnBoxRunning createRunning(String logPrefix, boolean withHook) {
        return createRunning(logPrefix, withHook, null, null, null);
    }

    public WnBoxRunning createRunning(String logPrefix,
                                      boolean withHook,
                                      OutputStream out,
                                      OutputStream err,
                                      InputStream ins) {
        return new WnBoxRunning(logPrefix, io, services, allocTimeout, withHook, out, err, ins);
    }

    public void exec(String logPrefix,
                     WnAuthSession se,
                     String cmdText,
                     OutputStream out,
                     OutputStream err,
                     InputStream in,
                     Callback<WnBoxContext> on_before_free) {

        // 得到一个沙箱
        WnBox box = boxes.alloc(allocTimeout);

        // 开始计时
        Stopwatch sw = null;
        if (boxLog.isDebugEnabled()) {
            sw = Stopwatch.begin();

            if (boxLog.isTraceEnabled()) {
                boxLog.tracef("%sbox:alloc: %s", logPrefix, box.id());
            }
        }

        // 保存到请求属性中，box.onClose 的时候会删除这个属性
        // req.setAttribute(WnBox.class.getName(), box);

        // 设置沙箱
        WnBoxContext bc = createBoxContext(se);

        if (boxLog.isTraceEnabled())
            boxLog.tracef("%sbox:setup: %s", logPrefix, bc);
        box.setup(bc);

        // 准备回调
        if (boxLog.isTraceEnabled())
            boxLog.tracef("%sbox:set stdin/out/err", logPrefix);

        box.setStdin(in);
        box.setStdout(out);
        box.setStderr(err);
        box.onBeforeFree(on_before_free);

        // 运行
        if (boxLog.isInfoEnabled())
            boxLog.infof("%sbox:run: %s", logPrefix, Ws.trim(cmdText));

        box.run(cmdText);

        // 释放沙箱
        if (boxLog.isTraceEnabled())
            boxLog.tracef("%sbox:free: %s", logPrefix, box.id());
        boxes.free(box);

        if (boxLog.isDebugEnabled()) {
            sw.stop();
            boxLog.debugf("%sbox:done : %dms", logPrefix, sw.getDuration());
        }
    }

    protected WnBoxContext createBoxContext(WnSession se) {
        WnBoxContext bc = new WnBoxContext(services, new NutMap());
        bc.io = io;
        bc.session = se;
        return bc;
    }

    public void runWithHook(WnUser usr, String grp, NutMap env, Callback<WnSession> callback) {
        long du = login.getSessionDuration(false);
        WnSession se = login.createSession(usr, du);
        try {
            // 附加环境变量
            if (env != null) {
                se.getEnv().putAll(env);
            }
            // 执行
            this.runWithHook(se, callback);
        }
        finally {
            login.removeSession(se);
        }
    }

    public void runWithHook(WnSession se, Callback<WnSession> callback) {
        WnBoxContext bc = new WnBoxContext(services, new NutMap());
        bc.io = io;
        bc.session = se;
        WnHookContext hc = new WnHookContext(boxes, bc);
        hc.service = hooks;

        WnContext ctx = Wn.WC();
        ctx.setSession(se);
        ctx.core(new WnSecurityImpl(io, auth), false, hc, new Atom() {
            public void run() {
                ctx.security(new WnSecurityImpl(io, auth), () -> callback.invoke(se));
            }
        });
    }

    public static <T> void sudo(WnSystem sys, Atom atom) {
        Wn.WC().nosecurity(sys.io, atom);
    }

    /**
     * 进入内核态执行操作
     * 
     * @param atom
     *            操作
     */
    public void nosecurity(Atom atom) {
        Wn.WC().nosecurity(io, atom);
    }

    /**
     * 进入内核态执行带返回的操作
     * 
     * @param protom
     *            操作
     * 
     * @return 返回结果
     */
    public <T> T nosecurity(Proton<T> proton) {
        return Wn.WC().nosecurity(io, proton);
    }

    public WnSysConf getSysConf() {
        return Wn.getSysConf(io);
    }
}
