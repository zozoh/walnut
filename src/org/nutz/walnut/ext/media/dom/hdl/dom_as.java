package org.nutz.walnut.ext.media.dom.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.ext.media.dom.DomContext;
import org.nutz.walnut.ext.media.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class dom_as extends DomFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(doc|selected|quiet)$");
    }

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        // 输出类型
        String type = params.val(0, "html");
        String output;

        // 强制静默
        if (params.is("quiet")) {
            fc.quiet = true;
            return;
        }

        // 准备输出的节点
        if (!params.is("selected") && (!fc.hasSelected() || params.is("doc"))) {
            // 输出超文本标签
            if ("html".equals(type)) {
                output = fc.doc.toHtml();
            }
            // 输出JSON
            else if ("json".equals(type)) {
                NutBean bean = fc.doc.root().toBean();
                JsonFormat jfmt = Cmds.gen_json_format(params);
                output = Json.toJson(bean, jfmt);
            }
            // 输出树形结构
            else if ("tree".equals(type)) {
                output = fc.doc.toString();
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
            // 输出超文本标签
            else if ("inner".equals(type)) {
                output = "";
                for (CheapElement ele : fc.selected) {
                    output += ele.toInnerMarkup() + "\n";
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
            // 输出树形结构
            else if ("tree".equals(type)) {
                List<String> marks = new ArrayList<>(fc.selected.size());
                for (CheapElement ele : fc.selected) {
                    marks.add(ele.toString());
                }
                // 只有一个，拆包
                if (marks.size() == 1) {
                    output = marks.get(0);
                }
                // 输出分隔符
                else {
                    output = "";
                    String HR = Ws.repeat('-', 60);
                    for (int i = 0; i < marks.size(); i++) {
                        output += String.format("%s\nItem[%s]:\n", HR, i);
                        output += marks.get(i);
                        output += "\n";
                    }
                }
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
