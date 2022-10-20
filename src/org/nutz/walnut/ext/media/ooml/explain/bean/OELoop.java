package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OELoop extends OENode {

    public OELoop() {
        this.type = OENodeType.LOOP;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {}

}
