package com.site0.walnut.api.hook;

import java.io.InputStream;
import java.io.OutputStream;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.stream.VoidInputStream;
import org.nutz.log.Log;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.util.Wlog;

public class WnHookContext {

    private static final Log log = Wlog.getHOOK();

    public WnHookContext(WnBoxService boxes, WnBoxContext bc) {
        this._boxes = boxes;
        this._bc = bc;
    }

    protected WnBoxContext _bc;

    protected WnBoxService _boxes;

    public WnHookService service;

    public void exec(String cmdText) {
        exec(cmdText, null, null, null);
    }

    @SuppressWarnings("resource")
    public void exec(String cmdText, InputStream stdin, OutputStream stdout, OutputStream stderr) {
        WnBox box = _boxes.alloc(0);

        // 准备上下文
        WnBoxContext bc = this._bc.clone();

        // 设置沙箱
        box.setup(bc);

        StringBuilder out = new StringBuilder();
        OutputStream dftOut = Wlang.ops(out);
        InputStream dftIn = new VoidInputStream();

        // 设置标准输入输出
        box.setStderr(stderr == null ? dftOut : stderr);
        box.setStdout(stdout == null ? dftOut : stdout);
        box.setStdin(stdin == null ? dftIn : stdin);

        // 执行
        box.run(cmdText);

        if (log.isInfoEnabled()) {
            log.infof("command: %s :>\n%s", cmdText, out);
        }

        // 释放
        _boxes.free(box);

    }

    public WnHookContext clone() {
        WnHookContext hc = new WnHookContext(_boxes, _bc);
        hc.service = service;
        return hc;
    }

    public WnAuthService auth() {
        return _bc.auth;
    }

    public WnIo io() {
        return _bc.io;
    }

    public WnAuthSession getSession() {
        return this._bc.session;
    }

    public void setSession(WnAuthSession se) {
        this._bc.session = se;
    }

}
