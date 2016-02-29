package org.nutz.walnut.ext.hmaker.hdl;

import org.nutz.lang.Files;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("Qf")
public class hmaker_init implements JvmHdl {

    private String components_html;

    public hmaker_init() {
        this.components_html = Files.read("org/nutz/walnut/ext/hmaker/hdl/components.html");
    }

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        boolean show_log = !hc.params.is("Q");

        // 打印处理目录
        if (show_log) {
            sys.out.println("hmaker home : " + hc.oHome.path());
        }

        // 确保主目录有组件加载项
        String aph = Wn.normalizeFullPath("~/.hmaker/components.html", sys);
        WnObj oComs = sys.io.fetch(null, aph);

        // 不存在就创建
        if (oComs == null) {
            oComs = sys.io.createIfNoExists(null, aph, WnRace.FILE);
            if (show_log) {
                sys.out.println(" + ~/.hmaker/components.html");
            }
        }

        // 如果空文件或者强制重写，就写一下
        if (oComs.len() == 0 || hc.params.is("f")) {
            sys.io.writeText(oComs, this.components_html);
            if (show_log) {
                sys.out.println(" >>> reset: ~/.hmaker/components.html");
            }
        }

        // 打印结束
        if (show_log) {
            sys.out.println("All Done");
        }

    }

}
