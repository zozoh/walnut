package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OECopyText extends OEItem {

    public static OECopyText create(String text) {
        OECopyText ct = new OECopyText();
        ct.setText(text);
        return ct;
    }

    private String text;

    public OECopyText() {
        this.type = OENodeType.COPY_TEXT;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {
        pEl.setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
