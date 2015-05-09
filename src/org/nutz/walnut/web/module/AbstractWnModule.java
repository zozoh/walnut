package org.nutz.walnut.web.module;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.box.Jvms;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.WnConfig;

public abstract class AbstractWnModule {

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

    protected String _run_cmd(String logPrefix, WnSession se, String input, String cmdText) {
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        OutputStream out = Lang.ops(sbOut);
        OutputStream err = Lang.ops(sbErr);
        InputStream in = null == input ? null : Lang.ins(input);

        _run_cmd(logPrefix, se, cmdText, out, err, in);

        if (sbErr.length() > 0)
            throw Er.create("e.cmd.error", sbErr);

        return sbOut.toString();
    }

    protected void _run_cmd(String logPrefix,
                            WnSession se,
                            String cmdText,
                            OutputStream out,
                            OutputStream err,
                            InputStream in) {
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

        // 运行
        if (log.isDebugEnabled())
            log.debugf("%sbox:run: %s", logPrefix, cmdText);
        
        String[] cmdLines = Jvms.split(cmdText, true, '\n', ';');
        for (String cmdLine : cmdLines) {
            box.submit(cmdLine);
        }
        box.run();

        // 释放沙箱
        if (log.isDebugEnabled())
            log.debugf("%sbox:free: %s", logPrefix, box.id());
        boxes.free(box);

        if (log.isDebugEnabled())
            log.debugf("%sbox:done", logPrefix);
    }

    protected WnObj _find_app_home(String appName) {
        String rpath = appName.replace('.', '/');
        String appPaths = Wn.WC().checkSE().envs().getString("APP_PATH");
        String[] bases = Strings.splitIgnoreBlank(appPaths, ":");
        for (String base : bases) {
            String ph = Wn.appendPath(base, rpath);
            WnObj o = io.fetch(null, ph);
            if (null != o)
                return o;
        }
        return null;
    }

    protected WnObj _check_app_home(String appName) {
        WnObj oAppHome = _find_app_home(appName);
        if (null == oAppHome)
            throw Er.create("e.app.noexists", appName);
        return oAppHome;
    }
}
