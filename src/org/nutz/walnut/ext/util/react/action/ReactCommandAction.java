package org.nutz.walnut.ext.util.react.action;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.Wn;

public class ReactCommandAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasPath() && !a.hasInput()) {
            return;
        }

        // 读取命令内容
        String tmpl = a.input;
        if (!a.hasInput()) {
            WnObj oIn = Wn.checkObj(r.io, r.session, a.path);
            tmpl = r.io.readText(oIn);
        }

        // 展开动作模板
        String cmdText = Tmpl.exec(tmpl, r.vars);

        // 执行命令
        r.runner.exec(cmdText);
    }

}
