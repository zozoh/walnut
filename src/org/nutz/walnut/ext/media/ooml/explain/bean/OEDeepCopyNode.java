package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OEDeepCopyNode extends OEItem {

    public static OEDeepCopyNode create(CheapElement el) {
        OEDeepCopyNode cn = new OEDeepCopyNode();
        cn.setRefer(el);
        return cn;
    }

    public OEDeepCopyNode() {
        this.type = OENodeType.DEEP_COPY_NODE;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        CheapElement el = this.refer.clone();

        // 加入父节点
        if (null != pEl) {
            pEl.append(el);
        }

        // 搞定后返回自己
        return el;
    }

}
