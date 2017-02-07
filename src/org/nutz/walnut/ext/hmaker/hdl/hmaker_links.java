package org.nutz.walnut.ext.hmaker.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("^(line|c|site)$")
public class hmaker_links implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到站点主目录
        WnObj oSiteHome = Hms.getSiteHome(sys, hc.oRefer);

        // 得到要比较的路径
        String basePath;
        if (hc.params.is("site")) {
            basePath = oSiteHome.path() + "/";
        }
        // 否则采用给定的参考路径
        else {
            basePath = hc.oRefer.path() + (hc.oRefer.isDIR() ? "/" : "");
        }

        // 准备返回结果
        List<WnObj> list = new LinkedList<>();

        // 得到要搜索的顶级目录
        String[] ignores = Strings.splitIgnoreBlank(hc.params.get("ignore", "css,js,lib,image"));
        List<WnObj> children = sys.io.getChildren(oSiteHome, null);
        for (WnObj o : children) {
            // 直接就是网页
            if (o.isFILE()) {
                __try_add(basePath, list, o);
            }
            // 如果不忽略的话，则深入查找
            else if (!Lang.contains(ignores, o.name())) {
                sys.io.walk(o, new Callback<WnObj>() {
                    public void invoke(WnObj obj) {
                        __try_add(basePath, list, obj);
                    }
                }, WalkMode.LEAF_ONLY);
            }
        }

        // 输出
        Hms.output_resource_objs(sys, hc, list);

    }

    private void __try_add(String basePath, List<WnObj> list, WnObj o) {
        if (o.isType("html")) {
            String rph = Disks.getRelativePath(basePath, o.path());
            if (!"./".equals(rph)) {
                o.setv("rph", rph);
                list.add(o);
            }
        }
    }

}
