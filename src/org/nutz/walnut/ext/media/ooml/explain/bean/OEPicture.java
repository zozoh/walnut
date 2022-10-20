package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OEPicture extends OEVarItem {

    public OEPicture() {
        this.type = OENodeType.PICTURE;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {}

}
