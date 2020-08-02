package org.nutz.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.hook.WnHookService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.impl.box.JvmExecutorFactory;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.web.WnConfig;

@IocBean
public class WnRun {

    private static final Log log = Logs.get();

    @Inject("refer:io")
    private WnIo io;

    @Inject("refer:sysAuthService")
    private WnAuthService auth;

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

    public WnIo io() {
        return io;
    }

    public WnAuthService auth() {
        return auth;
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
        final WnAccount u = auth.checkAccount(unm);
        final WnAuthSession se = auth.createSession(u, false);

        // 执行命令
        try {
            return exec(logPrefix, se, input, cmdText);
        }
        // 退出会话
        finally {
            auth.removeSession(se, 0);
        }
    }

    public void exec(String logPrefix,
                     String unm,
                     String input,
                     String cmdText,
                     StringBuilder sbOut,
                     StringBuilder sbErr) {
        // 检查用户和会话
        final WnAuthSession se = creatSession(unm, false);
        InputStream in = null == input ? null : Lang.ins(input);
        OutputStream out = Lang.ops(sbOut);
        OutputStream err = Lang.ops(sbErr);
        // 执行命令
        try {
            this.exec(logPrefix, se, cmdText, out, err, in, null);
        }
        // 退出会话
        finally {
            auth.removeSession(se, 0);
        }
    }

    public WnAuthSession creatSession(String unm, boolean longSession) {
        final WnAccount u = auth.checkAccount(unm);

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
        OutputStream out = Lang.ops(sbOut);
        OutputStream err = Lang.ops(sbErr);
        InputStream in = null == input ? null : Lang.ins(input);

        exec(logPrefix, se, cmdText, out, err, in, null);

        if (sbErr.length() > 0)
            throw Er.create("e.cmd.error", sbErr);

        return sbOut.toString();
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
        if (log.isDebugEnabled()) {
            sw = Stopwatch.begin();

            if (log.isTraceEnabled()) {
                log.tracef("%sbox:alloc: %s", logPrefix, box.id());
            }
        }

        // 保存到请求属性中，box.onClose 的时候会删除这个属性
        // req.setAttribute(WnBox.class.getName(), box);

        // 设置沙箱
        WnBoxContext bc = createBoxContext(se);

        if (log.isTraceEnabled())
            log.tracef("%sbox:setup: %s", logPrefix, bc);
        box.setup(bc);

        // 准备回调
        if (log.isTraceEnabled())
            log.tracef("%sbox:set stdin/out/err", logPrefix);

        box.setStdin(in);
        box.setStdout(out);
        box.setStderr(err);
        box.onBeforeFree(on_before_free);

        // 运行
        if (log.isInfoEnabled())
            log.infof("%sbox:run: %s", logPrefix, cmdText);

        box.run(cmdText);

        // 释放沙箱
        if (log.isTraceEnabled())
            log.tracef("%sbox:free: %s", logPrefix, box.id());
        boxes.free(box);

        if (log.isDebugEnabled()) {
            sw.stop();
            log.debugf("%sbox:done : %dms", logPrefix, sw.getDuration());
        }
    }

    protected WnBoxContext createBoxContext(WnAuthSession se) {
        WnBoxContext bc = new WnBoxContext(new NutMap());
        bc.io = io;
        bc.session = se;
        bc.auth = auth;
        return bc;
    }

    public void runWithHook(WnAccount usr,
                            String grp,
                            NutMap env,
                            Callback<WnAuthSession> callback) {
        WnAuthSession se = auth.createSession(usr, false);
        try {
            // 附加环境变量
            if (env != null) {
                se.getVars().putAll(env);
            }
            // 执行
            this.runWithHook(se, callback);
        }
        finally {
            auth.removeSession(se, 0);
        }
    }

    public void runWithHook(WnAuthSession se, Callback<WnAuthSession> callback) {
        WnBoxContext bc = new WnBoxContext(new NutMap());
        bc.io = io;
        bc.session = se;
        bc.auth = auth;
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
