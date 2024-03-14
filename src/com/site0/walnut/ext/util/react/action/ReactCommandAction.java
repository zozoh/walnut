package com.site0.walnut.ext.util.react.action;

import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;

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
        String cmdText = WnTmpl.exec(tmpl, r.vars);

        // 执行命令
        r.runner.exec(cmdText);
    }

}
