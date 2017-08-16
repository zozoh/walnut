package org.nutz.walnut.ext.hmaker.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("ocqn")
public class hmaker_newsite implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到创建的目标
        String taPh = hc.params.val_check(0);

        String aTaPh = Wn.normalizeFullPath(taPh, sys);

        // 执行创建
        WnObj oSite = sys.io.create(null, aTaPh, WnRace.DIR);
        oSite.type("hmaker_site");
        sys.io.set(oSite, "^tp$");

        // 执行 Copy
        if (hc.params.has("copy")) {
            WnObj oSrcSite = Wn.checkObj(sys, hc.params.check("copy"));
            // Copy 皮肤
            if (oSrcSite.has("hm_site_skin")) {
                oSite.put("hm_site_skin", oSrcSite.getString("hm_site_skin"));
                sys.io.set(oSite, "^hm_site_skin$");
            }

            // Copy 内容
            sys.exec("cp -rp id:" + oSrcSite.id() + " id:" + oSite.id());
        }
        // 否则初始化
        else {
            sys.io.create(oSite, "image", WnRace.DIR);
            WnObj oIndex = sys.io.create(oSite, "index", WnRace.FILE);
            oIndex.type("html").mime("text/html");
            sys.io.set(oIndex, "^(tp|mime)$");
        }

        // 是否输出
        if (hc.params.is("o")) {
            sys.out.println(Json.toJson(oSite, hc.jfmt));
        }
    }

}
