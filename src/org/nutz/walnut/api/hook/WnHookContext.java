package org.nutz.walnut.api.hook;

import org.nutz.lang.stream.VoidInputStream;
import org.nutz.lang.stream.VoidOutputStream;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;

public class WnHookContext {

    public WnHookContext(WnBoxService boxes, WnBoxContext bc) {
        this._boxes = boxes;
        this._bc = bc;
    }

    public WnIo io;

    public WnUsr me;

    public WnSession se;

    protected WnBoxContext _bc;

    protected WnBoxService _boxes;

    public WnHookService service;

    public void exec(String cmdText) {
        WnBox box = _boxes.alloc(0);

        // 设置沙箱
        box.setup(_bc);

        // 设置标准输入输出
        box.setStderr(new VoidOutputStream());
        box.setStdout(new VoidOutputStream());
        box.setStdin(new VoidInputStream());

        // 执行
        box.run(cmdText);

        // 释放
        _boxes.free(box);

    }

    public WnHookContext clone() {
        WnHookContext hc = new WnHookContext(_boxes, _bc);
        hc.io = io;
        hc.me = me;
        hc.se = se;
        hc.service = service;
        return hc;
    }

}
