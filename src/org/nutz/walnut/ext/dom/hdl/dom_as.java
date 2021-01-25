package org.nutz.walnut.ext.dom.hdl;

import org.nutz.walnut.ext.dom.DomContext;
import org.nutz.walnut.ext.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class dom_as extends DomFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(doc)$");
    }

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        // 输出类型
        String type = params.val(0, "html");
        String output;

        // 准备输出的节点
        if (null == fc.current || params.is("doc")) {
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
                output = fc.current.toMarkup();
            }
            // 输出文本节点
            else {
                output = Ws.trim(fc.current.getText());
            }
        }

        // 标识主程序静默
        fc.quiet = true;

        // 输出
        sys.out.println(output);
    }

}
