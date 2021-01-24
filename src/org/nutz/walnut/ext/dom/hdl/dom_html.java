package org.nutz.walnut.ext.dom.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.cheap.html.CheapHtmlParsing;
import org.nutz.walnut.ext.dom.DomContext;
import org.nutz.walnut.ext.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class dom_html extends DomFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(body)$");
    }

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        CheapHtmlParsing ing = new CheapHtmlParsing(params.is("body"));

        // 读取 HTML 输入
        String html;
        // 从文件读取
        if (params.vals.length > 0) {
            String ph = params.val_check(0);
            WnObj oHtml = Wn.checkObj(sys, ph);
            html = sys.io.readText(oHtml);
        }
        // 从标准输入读取
        else {
            html = sys.in.readAll();
        }

        // 开始解析
        fc.doc = ing.invoke(html);
        fc.current = null;
    }

}
