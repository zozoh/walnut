package org.nutz.walnut.ext.data.thing.util;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnObj;

public class ThingCommandProton extends Proton<String> {

    private WnObj oT;
    private NutMap valContext;
    private Tmpl cmdTmpl;

    public ThingCommandProton(WnObj oT, NutMap valContext, String cmdTmpl) {
        this.oT = oT;
        this.valContext = valContext;
        this.cmdTmpl = Tmpl.parse(cmdTmpl);
    }

    @Override
    protected String exec() {
        // 合成新的上下文
        NutMap map = new NutMap();

        // 合并 valContext
        map.putAll(valContext);

        // 合并更新后的数据对象
        if (null != oT) {
            map.putAll(oT);
        }

        // if (null != valContext) {
        // for (Map.Entry<String, Object> en : valContext.entrySet()) {
        // String key = en.getKey();
        // Object val = en.getValue();
        // map.put("@" + key, val);
        // }
        // }

        // 展开模板
        return cmdTmpl.render(map);
    }

}
