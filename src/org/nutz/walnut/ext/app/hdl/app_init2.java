package org.nutz.walnut.ext.app.hdl;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.impl.AppInitContext;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class app_init2 implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        AppInitContext aic = new AppInitContext();
        // 找到模板的源，会依次尝试
        // 0. vals[0]
        // 1. ~/.domain/init/_files
        // 2. /mnt/project/${domain}/init/domain/_files
        // 3. /mnt/project/${domain}/*/init/domain/_files
        loadHome(sys, hc, aic);

        // 找到操作目标目录，默认为 ~
        String phDist = hc.params.getString("dir", "~");
        aic.oDist = Wn.checkObj(sys, phDist);

        // 找到上下文变量，会依次尝试
        // 0. -vars
        // 1. [STDIN]
        // 2. ~/.domain/vars.json

        // 读取模板文件
        WnObj oInitFile = sys.io.check(aic.oHome, "_files");
        

        // 执行初始化

        // 找到后续脚本
        String scriptName = hc.params.getString("script", "_script");
        if (!"off".equals(scriptName)) {
            WnObj oScript = sys.io.fetch(aic.oHome, scriptName);
            if (null != oScript) {
                String script = sys.io.readText(oScript);
                script = Tmpl.exec(script, aic.vars);
                sys.out.printlnf("run script:\n%s", script);
                sys.exec(script);
            }
        }
    }

    private void loadHome(WnSystem sys, JvmHdlContext hc, AppInitContext aic) {
        String fName = hc.params.getString("by", "_files");
        
        // 找到模板的源，会依次尝试
        // 0. vals[0]
        String phHome = hc.params.val(0);
        if (!Strings.isBlank(phHome)) {
            String phFile = Wn.appendPath(phHome, fName);
            aic.oInitFile = Wn.checkObj(sys, phFile);
            aic.oHome = aic.oInitFile.parent();
            return;
        }

        // 1. ~/.domain/init/_files
        aic.oInitFile = Wn.getObj(sys, "~/.domain/init/_files");
        if (null != aic.oInitFile) {
            aic.oHome = aic.oInitFile.parent();
            return;
        }

        // 看看是否在全局映射里
        String grp = sys.getMyGroup();
        WnObj oMntHome = sys.io.check(null, "/mnt/project/" + grp);

        // 2. /mnt/project/${domain}/init/domain/_files
        aic.oInitFile = sys.io.fetch(oMntHome, "init/domain/_files");
        if (null != aic.oInitFile) {
            aic.oHome = aic.oInitFile.parent();
            return;
        }

        // 3. /mnt/project/${domain}/*/init/domain/_files
        List<WnObj> children = sys.io.getChildren(oMntHome, null);
        for (WnObj oChild : children) {
            if (!oChild.isDIR()) {
                continue;
            }
            aic.oInitFile = sys.io.fetch(oChild, "init/domain/_files");
            if (null != aic.oInitFile) {
                aic.oHome = aic.oInitFile.parent();
                return;
            }
        }

        throw Er.create("e.cmd.app_init.FailToFoundHome");
    }

}
