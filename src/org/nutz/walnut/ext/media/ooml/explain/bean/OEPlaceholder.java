package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OEPlaceholder extends OEVarItem {

    public OEPlaceholder() {
        this.type = OENodeType.PLACEHOLDER;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {}

}
