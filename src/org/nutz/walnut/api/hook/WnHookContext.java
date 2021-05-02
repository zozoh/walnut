package org.nutz.walnut.api.hook;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.stream.VoidInputStream;
import org.nutz.lang.stream.VoidOutputStream;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnIo;

public class WnHookContext {

    public WnHookContext(WnBoxService boxes, WnBoxContext bc) {
        this._boxes = boxes;
        this._bc = bc;
    }

    protected WnBoxContext _bc;

    protected WnBoxService _boxes;

    public WnHookService service;

    private static OutputStream VOID_OUT = new VoidOutputStream();
    private static InputStream VOID_IN = new VoidInputStream();

    public void exec(String cmdText) {
        exec(cmdText, null, null, null);
    }

    public void exec(String cmdText, InputStream stdin, OutputStream stdout, OutputStream stderr) {
        WnBox box = _boxes.alloc(0);

        // 准备上下文
        WnBoxContext bc = this._bc.clone();

        // 设置沙箱
        box.setup(bc);

        // 设置标准输入输出
        box.setStderr(stderr == null ? VOID_OUT : stderr);
        box.setStdout(stdout == null ? VOID_OUT : stdout);
        box.setStdin(stdin == null ? VOID_IN : stdin);

        // 执行
        box.run(cmdText);

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
