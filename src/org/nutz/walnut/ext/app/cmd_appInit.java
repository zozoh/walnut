package org.nutz.walnut.ext.app;

import java.io.Reader;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Context;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.bean.AppInfo;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_appInit extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        // 解析参数
        ZParams params = ZParams.parse(args, null);

        // 准备上下文
        Context c = Lang.context();
        c.set("host", params.get("host", "<nil>"));
        c.set("grp", params.get("grp", sys.se.group()));
        c.set("usr", sys.se.me());
        String pnb = params.get("pnb");
        c.set("pnb", pnb);
        if (params.has("img"))
            c.set("img", params.get("img"));

        // 读取并解析输入
        Reader reader = null;
        if (params.has("file")) {
            reader = Streams.fileInr(params.get("file"));
        } else {
            reader = sys.in.getReader();
        }

        AppInfo ai = new AppInfo();
        ai.parseAndClose(reader, c);

        // 得到目标目录
        WnObj taHome;
        if (params.vals.length > 0) {
            String val = params.vals[0];
            taHome = Wn.checkObj(sys, val);
            if (!taHome.isDIR()) {
                throw Er.create("e.cmd.app-init.taHomeNoDir", taHome);
            }
        }
        // 默认当前路径
        else {
            taHome = sys.getCurrentObj();
        }

        // 执行处理
        DoAppInit doai = new DoAppInit();
        doai.sys = sys;
        doai.c = c;
        doai.ai = ai;
        doai.taHome = taHome;

        doai.doIt();
    }

}
