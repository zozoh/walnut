package org.nutz.walnut.ext.util.react.action;

import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.JsExec;
import org.nutz.walnut.util.JvmJsExecContext;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wn;

public class ReactJsAction implements ReactActionHandler {

    private static final Log log = Wlog.getCMD();

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasPath()) {
            return;
        }

        // 准备必要的参数
        String engineName = JsExec.dft_engine_nm;

        // 准备变量
        NutMap vars = new NutMap();
        vars.putAll(r.vars);
        if (a.hasParams()) {
            vars.putAll(a.params);
        }
        vars.put("log", log);

        // 得到文件路径
        WnObj oJs = Wn.checkObj(r.io, r.session, a.path);
        String jsStr = r.io.readText(oJs);

        // 得到运行器
        JsExec JE = JsExec.me();

        // 创建上下文
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        JvmJsExecContext jc = new JvmJsExecContext(r.io, r.session, r.runner, out, err);

        // 执行
        try {
            JE.exec(jc, engineName, vars, jsStr);
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

}
