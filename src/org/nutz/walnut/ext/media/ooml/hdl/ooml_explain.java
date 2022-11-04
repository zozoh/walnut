package org.nutz.walnut.ext.media.ooml.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.impl.WnCheapResourceLoader;
import org.nutz.walnut.ext.media.ooml.OomlContext;
import org.nutz.walnut.ext.media.ooml.OomlFilter;
import org.nutz.walnut.ext.media.ooml.api.OomlExplaining;
import org.nutz.walnut.ext.media.ooml.explain.WnOomlDocxExplaining2;
import org.nutz.walnut.ext.media.ooml.impl.WnOomlDocxExplaining;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class ooml_explain extends OomlFilter {

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        // 准备参数： 变量集
        String varJson;
        if (params.has("in")) {
            varJson = params.getString("in");
        }
        // 从标准输入读取
        else {
            varJson = sys.in.readAll();
        }
        NutMap vars = Ws.isBlank(varJson) ? new NutMap() : Wlang.map(varJson);

        // 准备处理方法接口
        CheapResourceLoader loader = new WnCheapResourceLoader(sys);
        OomlExplaining api = new WnOomlDocxExplaining2(fc.ooml, loader);

        // 执行
        api.explain(vars);
    }

}
