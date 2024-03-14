package com.site0.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.util.Ws;

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

    public String toBrief() {
        String name = Ws.camelCase(type.toString());
        name = Ws.upperFirst(name);
        return String.format("{ %s } : '%s'", name, text);
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        pEl.setText(text);
        return pEl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
