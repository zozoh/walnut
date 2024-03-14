package com.site0.walnut.ext.media.ooml.explain.bean;

import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.cheap.dom.CheapElement;

public class OEDeepCopyNode extends OENode {

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

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public void addChild(OEItem node) {}

    @Override
    public void clearChildren() {}

    @Override
    public List<OEItem> getChildren() {
        return null;
    }

}
