package com.site0.walnut.ext.media.dom.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.ext.media.dom.DomContext;
import com.site0.walnut.ext.media.dom.DomFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class dom_transattr extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        // 属性名称
        String attrName = params.check("attr");
        // 分析映射
        String mpstr = Cmds.checkParamOrPipe(sys, params, "mapping", true);

        NutMap mapping;
        if (Ws.isQuoteBy(mpstr, '{', '}')) {
            mapping = Json.fromJson(NutMap.class, mpstr);
        }
        // 来自文件
        else {
            WnObj oMapping = Wn.checkObj(sys, mpstr);
            mapping = sys.io.readJson(oMapping, NutMap.class);
        }

        // 逐个选择器处理 Dom
        for (String selector : params.vals) {
            List<CheapElement> els = fc.doc.selectAll(selector);
            for (CheapElement el : els) {
                String val = el.attr(attrName);
                String v2 = mapping.getString(val);
                if (null != v2) {
                    el.attr(attrName, v2);
                }
            }
        }
    }

}
