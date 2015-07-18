package org.nutz.walnut.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.web.WnConfig;

@IocBean
public class WnRun {

    private static final Log log = Logs.get();

    @Inject("refer:conf")
    protected WnConfig conf;

    @Inject("refer:io")
    protected WnIo io;

    @Inject("refer:sessionService")
    protected WnSessionService sess;

    @Inject("refer:usrService")
    protected WnUsrService usrs;

    @Inject("refer:boxService")
    protected WnBoxService boxes;

    @Inject("java:$conf.getInt('box-alloc-timeout')")
    protected int allocTimeout;

    @Inject("refer:mimes")
    protected MimeMap mimes;

    public String exec(final String logPrefix, String unm, final String cmdText) {
        // 检查用户和会话
        final WnUsr u = usrs.check(unm);
        final WnSession se = Wn.WC().su(u, new Proton<WnSession>() {
            protected WnSession exec() {
                return sess.create(u);
            }
        });
        // 执行命令
        try {
            return exec(logPrefix, se, null, cmdText);
        }
        // 退出会话
        finally {
            sess.logout(se.id());
        }
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

        if (log.isDebugEnabled())
            log.debugf("%sbox:alloc: %s", logPrefix, box.id());

        // 保存到请求属性中，box.onClose 的时候会删除这个属性
        // req.setAttribute(WnBox.class.getName(), box);

        // 设置沙箱
        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = usrs.check(se.me());
        bc.session = se;
        bc.usrService = usrs;
        bc.sessionService = sess;

        if (log.isDebugEnabled())
            log.debugf("%sbox:setup: %s", logPrefix, bc);
        box.setup(bc);

        // 准备回调
        if (log.isDebugEnabled())
            log.debugf("%sbox:set stdin/out/err", logPrefix);

        box.setStdin(in);
        box.setStdout(out);
        box.setStderr(err);
        box.onBeforeFree(on_before_free);

        // 运行
        if (log.isDebugEnabled())
            log.debugf("%sbox:run: %s", logPrefix, cmdText);

        box.run(cmdText);

        // 释放沙箱
        if (log.isDebugEnabled())
            log.debugf("%sbox:free: %s", logPrefix, box.id());
        boxes.free(box);

        if (log.isDebugEnabled())
            log.debugf("%sbox:done", logPrefix);
    }
}
