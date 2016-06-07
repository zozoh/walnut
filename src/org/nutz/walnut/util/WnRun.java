package org.nutz.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.hook.WnHookService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;

@IocBean
public class WnRun {

    private static final Log log = Logs.get();

    @Inject("refer:io")
    protected WnIo io;

    @Inject("refer:sessionService")
    protected WnSessionService sess;

    @Inject("refer:usrService")
    protected WnUsrService usrs;

    @Inject("refer:boxService")
    protected WnBoxService boxes;
    
    @Inject("refer:hookService")
    protected WnHookService hooks;

    @Inject("java:$conf.getInt('box-alloc-timeout')")
    protected int allocTimeout;

    public WnIo io() {
        return io;
    }
    
    public WnSessionService sess() {
        return sess;
    }
    
    public WnUsrService usrs() {
        return usrs;
    }
    
    public WnBoxService boxes() {
        return boxes;
    }
    
    public WnHookService hooks() {
        return hooks;
    }

    public String exec(String logPrefix, String unm, final String cmdText) {
        return exec(logPrefix, unm, null, cmdText);
    }

    public String exec(String logPrefix, String unm, String input, String cmdText) {
        // 检查用户和会话
        final WnUsr u = usrs.check(unm);
        final WnSession se = Wn.WC().su(u, new Proton<WnSession>() {
            protected WnSession exec() {
                return sess.create(u);
            }
        });
        // 执行命令
        try {
            return exec(logPrefix, se, input, cmdText);
        }
        // 退出会话
        finally {
            sess.logout(se.id());
        }
    }

    public void exec(String logPrefix,
                     String unm,
                     String input,
                     String cmdText,
                     StringBuilder sbOut,
                     StringBuilder sbErr) {
        // 检查用户和会话
        final WnSession se = creatSession(unm);
        InputStream in = null == input ? null : Lang.ins(input);
        OutputStream out = Lang.ops(sbOut);
        OutputStream err = Lang.ops(sbErr);
        // 执行命令
        try {
            this.exec(logPrefix, se, cmdText, out, err, in, null);
        }
        // 退出会话
        finally {
            sess.logout(se.id());
        }
    }

    public WnSession creatSession(String unm) {
        final WnUsr u = usrs.check(unm);

        // zozoh: 为啥？考，应该直接创建就好了吧 ...
        // return Wn.WC().su(u, new Proton<WnSession>() {
        // protected WnSession exec() {
        // return sess.create(u);
        // }
        // });

        return sess.create(u);
    }

    public String exec(String logPrefix, WnSession se, String cmdText) {
        return exec(logPrefix, se, null, cmdText);
    }

    public String exec(String logPrefix, WnSession se, String input, String cmdText) {
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
                     WnSession se,
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
        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = usrs.check(se.me());
        bc.session = se;
        bc.usrService = usrs;
        bc.sessionService = sess;

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
    
    public void runWithHook(WnSession se, WnUsr usr, String grp, NutMap env, Callback<WnSession> callback) {
        
        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = usr;
        bc.session = se;
        bc.usrService = usrs;
        bc.sessionService = sess;
        WnHookContext hc = new WnHookContext(boxes, bc);
        hc.io = io;
        hc.me = usr;
        hc.se = se;
        hc.service = hooks;

        if (env != null) {
            for (Entry<String, Object> en : env.entrySet()) {
                se.var(en.getKey(), en.getValue());
            }
        }

        Wn.WC().me(usr.name(), Strings.sBlank(grp, usr.name()));
        Wn.WC().hooking(hc, new Atom() {
            public void run() {
                callback.invoke(se);
            }
        });
        
    }
}
