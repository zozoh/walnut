package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OECopyText extends OEItem {

    private String text;

    public OECopyText() {
        this.type = OENodeType.COPY_TEXT;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {
        pEl.setText(text);
    }

}
