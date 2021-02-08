package org.nutz.walnut.ext.dom.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.ext.dom.DomContext;
import org.nutz.walnut.ext.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class dom_as extends DomFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(doc|selected)$");
    }

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        // 输出类型
        String type = params.val(0, "html");
        String output;

        // 准备输出的节点
        if (!params.is("selected") && (!fc.hasSelected() || params.is("doc"))) {
            // 输出超文本标签
            if ("html".equals(type)) {
                output = fc.doc.toHtml();
            }
            // 输出文本节点
            else {
                output = Ws.trim(fc.doc.root().getText());
            }
        }
        // 输出当前节点
        else {
            // 输出超文本标签
            if ("html".equals(type)) {
                output = "";
                for (CheapElement ele : fc.selected) {
                    output += ele.toMarkup() + "\n";
                }
            }
            // 输出JSON
            else if ("json".equals(type)) {
                List<NutBean> beans = new ArrayList<>(fc.selected.size());
                for (CheapElement ele : fc.selected) {
                    beans.add(ele.toBean());
                }
                JsonFormat jfmt = Cmds.gen_json_format(params);
                output = Json.toJson(beans, jfmt);
            }
            // 输出文本节点
            else {
                output = "";
                for (CheapElement ele : fc.selected) {
                    output += ele.getText() + "\n";
                }
            }
        }

        // 标识主程序静默
        fc.quiet = true;

        // 输出
        sys.out.println(output);
    }

}
