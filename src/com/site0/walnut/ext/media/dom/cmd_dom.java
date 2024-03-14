package com.site0.walnut.ext.media.dom;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_dom extends JvmFilterExecutor<DomContext, DomFilter> {

    public cmd_dom() {
        super(DomContext.class, DomFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(body)$");
    }

    @Override
    protected DomContext newContext() {
        return new DomContext();
    }

    @Override
    protected void prepare(WnSystem sys, DomContext fc) {
        fc.loadHtml(fc.params.vals, true, fc.params.is("body"));
    }

    @Override
    protected void output(WnSystem sys, DomContext fc) {
        if (!fc.quiet) {
            String output;

            // 准备输出的节点
            if (!fc.hasSelected()) {
                output = fc.doc.toHtml();
            }
            // 输出当前节点
            else {
                output = "";
                for (CheapElement ele : fc.selected) {
                    output += ele.toMarkup() + "\n";
                }
            }

            // 输出
            sys.out.println(output);
        }
    }

}
