package com.site0.walnut.ext.media.ooml.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.cheap.api.CheapResourceLoader;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.impl.WnCheapResourceLoader;
import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.ext.media.ooml.explain.WnOomlDocxExplaining2;
import com.site0.walnut.ext.media.ooml.explain.bean.OENode;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class ooml_explain extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(json)$");
    }

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
        WnOomlDocxExplaining2 api = new WnOomlDocxExplaining2(fc.ooml, loader);

        // 调试
        String debugEntry = params.get("debug");
        if ("true".equals(debugEntry)) {
            debugEntry = "word/document.xml";
        }
        if (!Ws.isBlank(debugEntry)) {
            OomlEntry en = fc.ooml.getEntry(debugEntry);
            if (null == en) {
                sys.err.printlnf("Entry NoExists: %s", debugEntry);
                return;
            } else {
                CheapDocument doc = api.parseEntryDocument(en);
                OENode rootNode = api.prepareRenderNode(doc);
                if (params.is("json")) {
                    JsonFormat jfmt = Cmds.gen_json_format(params);
                    String json = Json.toJson(rootNode, jfmt);
                    sys.out.println(json);
                } else {
                    sys.out.println(rootNode);
                }
            }
        }
        // 执行
        else {
            api.explain(vars);
        }
    }

}
