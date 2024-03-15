package com.site0.walnut.ext.data.app.hdl;

import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.app.impl.AppInitContext;
import com.site0.walnut.ext.data.app.impl.AppInitService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class app_init implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到当前操作路径
        String pwd = sys.session.getVars().getString("PWD");

        // 准备基石
        Stopwatch sw = Stopwatch.begin();

        // 准备上下文
        AppInitContext ac = new AppInitContext();
        // 找到模板的源，会依次尝试
        // 0. vals[0]
        // 1. ~/.domain/init/_files
        // 2. /mnt/project/${domain}/init/domain/_files
        // 3. /mnt/project/${domain}/*/init/domain/_files
        loadHome(sys, hc, ac);

        // 找到操作目标目录，默认为 ~
        String phDist = hc.params.getString("dir", "~");
        ac.oDist = Wn.checkObj(sys, phDist);

        // 找到上下文变量，会依次尝试
        // 0. -vars
        String varJson = hc.getString("vars");
        // 1. [STDIN]
        if (Strings.isBlank(varJson)) {
            varJson = sys.in.readAll();
        }
        // 2. ~/.domain/vars.json
        if (Strings.isBlank(varJson)) {
            WnObj oVar = Wn.getObj(sys, "~/.domain/vars.json");
            if (null != oVar) {
                varJson = sys.io.readText(oVar);
            } else {
                varJson = "{}";
            }
        }
        ac.vars = Wlang.map(varJson);

        // 准备服务类
        AppInitService init = new AppInitService();

        // 读取模板文件
        String input = sys.io.readText(ac.oInitFile);
        ac.group = init.parse(input, ac.vars);

        // 执行初始化
        ac.io = sys.io;
        ac.run = sys;
        ac.out = sys.out;
        ac.init = init;
        init.process(ac);

        // 找到后续脚本
        String scriptName = hc.params.getString("script", "_script");
        if (!"off".equals(scriptName)) {
            WnObj oScript = sys.io.fetch(ac.oHome, scriptName);
            if (null != oScript) {
                String script = sys.io.readText(oScript);
                script = WnTmpl.exec(script, ac.vars);
                sys.out.printlnf("run script:\n%s", script);
                sys.exec(script);
            }
        }

        // 恢复到当前操作路径
        sys.exec("cd '" + pwd + "'");

        // 结束计时
        sw.stop();
        sys.out.printlnf("All done : %s", sw.toString());
    }

    private void loadHome(WnSystem sys, JvmHdlContext hc, AppInitContext ac) {
        String fName = hc.params.getString("by", "_files");
        if (!"_files".equals(fName) && !fName.endsWith(".init")) {
            fName += ".init";
        }

        // 找到模板的源，会依次尝试
        // 0. vals[0]
        String phHome = hc.params.val(0);
        if (!Strings.isBlank(phHome)) {
            String phFile = Wn.appendPath(phHome, fName);
            ac.oInitFile = Wn.checkObj(sys, phFile);
            ac.oHome = ac.oInitFile.parent();
            return;
        }

        // 1. ~/.domain/init/_files
        ac.oInitFile = Wn.getObj(sys, "~/.domain/init/" + fName);
        if (null != ac.oInitFile) {
            ac.oHome = ac.oInitFile.parent();
            return;
        }

        // 2. ~/.domain/init/${grp}.init
        String grp = sys.getMyGroup();
        ac.oInitFile = Wn.getObj(sys, "~/.domain/init/" + grp + ".init");
        if (null != ac.oInitFile) {
            ac.oHome = ac.oInitFile.parent();
            return;
        }

        // 看看是否在全局映射里
        String initName = sys.getMe().getMetaString("init_name", grp);
        WnObj oMntHome = sys.io.check(null, "/mnt/project/" + initName);

        // 2. /mnt/project/${domain}/init/domain/_files
        ac.oInitFile = sys.io.fetch(oMntHome, "init/domain/" + fName);
        if (null != ac.oInitFile) {
            ac.oHome = ac.oInitFile.parent();
            return;
        }

        // 3. /mnt/project/${domain}/*/init/domain/_files
        List<WnObj> children = sys.io.getChildren(oMntHome, null);
        for (WnObj oChild : children) {
            if (!oChild.isDIR()) {
                continue;
            }
            ac.oInitFile = sys.io.fetch(oChild, "init/domain/" + fName);
            if (null != ac.oInitFile) {
                ac.oHome = ac.oInitFile.parent();
                return;
            }
        }

        throw Er.create("e.cmd.app_init.FailToFoundHome");
    }

}
