package org.nutz.walnut.ext.media.ooml.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.ext.media.ooml.OomlContext;
import org.nutz.walnut.ext.media.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class ooml_view extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(meta)$");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        OomlEntry en = fc.currentEntry;
        String enPath = params.val(0);
        if (!Ws.isBlank(enPath)) {
            en = fc.ooml.getEntry(enPath);
        }

        if (null == en) {
            sys.err.println("Nil Current Entry");
            return;
        }
        // 分析参数
        boolean showMeta = params.is("meta");
        String as = params.getString("as");

        // 打印当前条目摘要
        if (showMeta) {
            NutBean bean = en.toBean();
            bean.remove("content");
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String metaJson = Json.toJson(bean, jfmt);
            sys.out.printlnf(metaJson);
        }
        // 打印分割线
        if (!Ws.isBlank(as)) {
            String HR = Ws.repeat('-', 40);
            sys.out.println(HR);

            // 打印当前条目内容
            String str = en.getContentStr();
            if ("str".equals(as)) {
                sys.out.println(str);
            }
            // 作为 XML 打印
            else if ("xml".equals(as)) {
                CheapDocument doc = new CheapDocument(null);
                CheapXmlParsing ing = new CheapXmlParsing(doc);
                doc = ing.parseDoc(str);
                doc.formatAsXml();
                String xml = doc.toMarkup();
                sys.out.println(xml);
            }
            // 作为 XML 树打印
            else if ("tree".equals(as)) {
                CheapDocument doc = new CheapDocument(null);
                CheapXmlParsing ing = new CheapXmlParsing(doc);
                doc = ing.parseDoc(str);
                String tree = doc.toString();
                sys.out.println(tree);
            }
        }
    }

}
