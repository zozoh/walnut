package com.site0.walnut.impl.srv;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.WnAuthExecutable;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;

public class WnBoxRunning implements WnAuthExecutable {

    private static final Log log = Wlog.getBOX();

    private String logPrefix;

    private WnServiceFactory services;

    private WnBoxContext bc;

    private WnHookContext hc;

    private int boxAllocTimeout;

    private OutputStream stdOut;

    private OutputStream stdErr;

    private InputStream stdIns;

    public WnBoxRunning(String logPrefix,
                        WnIo io,
                        WnServiceFactory services,
                        int boxAllocTimeout,
                        boolean withHook) {
        this(logPrefix, io, services, boxAllocTimeout, withHook, null, null, null);
    }

    public WnBoxRunning(String logPrefix,
                        WnIo io,
                        WnServiceFactory services,
                        int boxAllocTimeout,
                        boolean withHook,
                        OutputStream out,
                        OutputStream err,
                        InputStream ins) {
        this.logPrefix = Ws.sBlank(logPrefix, "");
        this.services = services;
        this.boxAllocTimeout = boxAllocTimeout;
        this.bc = new WnBoxContext(services, new NutMap());
        this.bc.io = io;
        this.bc.auth = services.getAuthApi();
        this.stdOut = out;
        this.stdErr = err;
        this.stdIns = ins;
        if (withHook) {
            this.hc = new WnHookContext(services.getBoxApi(), bc);
            this.hc.service = services.getHookApi();
        }
    }

    @Override
    public void exec(String cmdText, OutputStream out, OutputStream err, InputStream ins) {
        // 未设置会话
        if (null == bc.session) {
            throw Er.create("e.box.running.withoutSession");
        }

        // 得到关键的 API
        WnBoxService boxes = services.getBoxApi();

        // 执行
        WnContext wc = Wn.WC();
        WnAuthSession oldSe = wc.getSession();
        WnHookContext oldHc = wc.getHookContext();
        WnSecurity oldSecu = wc.getSecurity();
        boolean oldSyncOff = wc.isSynctimeOff();
        try {
            // 准备线程上下文
            WnSecurity secu = new WnSecurityImpl(bc.io, bc.auth);
            wc.setSession(bc.session);
            wc.setHookContext(this.hc);
            wc.setSecurity(secu);
            wc.setSynctimeOff(false);

            // 得到一个沙箱
            WnBox box = boxes.alloc(this.boxAllocTimeout);

            // 开始计时
            Stopwatch sw = null;
            if (log.isDebugEnabled()) {
                sw = Stopwatch.begin();

                if (log.isTraceEnabled()) {
                    log.tracef("%sbox:alloc: %s", logPrefix, box.id());
                }
            }

            // 设置沙箱上下文
            if (log.isTraceEnabled())
                log.tracef("%sbox:setup: %s", logPrefix, bc);
            box.setup(this.bc);

            box.setStdin(ins);
            box.setStdout(out);
            box.setStderr(err);

            // 执行命令
            if (log.isInfoEnabled())
                log.infof("%sbox:run: %s", logPrefix, Ws.trim(cmdText));

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
        // 恢复原始线程设置
        finally {
            wc.setSession(oldSe);
            wc.setHookContext(oldHc);
            wc.setSecurity(oldSecu);
            wc.setSynctimeOff(oldSyncOff);
        }
    }

    @Override
    public void exec(String cmdText,
                     StringBuilder stdOut,
                     StringBuilder stdErr,
                     CharSequence stdIn) {
        InputStream ins = null == stdIn ? null : Lang.ins(stdIn);
        OutputStream out = null == stdOut ? null : Lang.ops(stdOut);
        OutputStream err = null == stdErr ? null : Lang.ops(stdErr);
        exec(cmdText, out, err, ins);
    }

    @Override
    public void exec(String cmdText) {
        exec(cmdText, stdOut, stdErr, stdIns);
    }

    @Override
    public void execf(String fmt, Object... args) {
        String cmdText = String.format(fmt, args);
        exec(cmdText);
    }

    @Override
    public void exec(String cmdText, CharSequence input) {
        InputStream ins = null == input ? null : Lang.ins(input);
        exec(cmdText, null, null, ins);
    }

    @Override
    public String exec2(String cmdText) {
        return exec2(cmdText, null);
    }

    @Override
    public String exec2f(String fmt, Object... args) {
        String cmdText = String.format(fmt, args);
        return exec2(cmdText);
    }

    @Override
    public String exec2(String cmdText, CharSequence input) {
        StringBuilder sb = new StringBuilder();
        exec(cmdText, sb, sb, input);
        return sb.toString();
    }

    @Override
    public void switchUser(WnAccount newUsr, Callback<WnAuthExecutable> callback) {
        WnAuthService auth = services.getAuthApi();

        // 只有当前会话不同，才要切换
        if (bc.session == null || !bc.session.getMe().isSame(newUsr)) {
            bc.session = auth.createSession(newUsr, true);
        }
        // 执行吧
        callback.invoke(this);
    }

}
