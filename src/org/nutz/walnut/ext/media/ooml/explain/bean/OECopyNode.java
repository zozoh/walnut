package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OECopyNode extends OENode {

    public static OECopyNode create(CheapElement el) {
        OECopyNode cn = new OECopyNode();
        cn.setRefer(el);
        return cn;
    }

    public OECopyNode() {
        this.type = OENodeType.COPY_NODE;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {
        CheapElement el = this.refer.cloneSelf();
        pEl.append(el);

        // 循环子节点
        if (this.hasChildren()) {
            for (OEItem it : this.children) {
                it.renderTo(el, vars);
            }
        }
    }

}
