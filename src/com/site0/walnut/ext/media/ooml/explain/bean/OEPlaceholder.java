package com.site0.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;

public class OEPlaceholder extends OEVarItem {

    private String dftValue;

    public OEPlaceholder() {
        this.type = OENodeType.PLACEHOLDER;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        Object v = Mapl.cell(vars, varName);
        String s = null == v ? dftValue : v.toString();
        CheapElement rPr = refer.clone();
        CheapDocument doc = pEl.getOwnerDocument();
        CheapElement r = doc.createElement("w:r");
        rPr.appendTo(r);
        CheapElement t = doc.createElement("w:t");
        t.setText(s);
        t.appendTo(r);
        r.appendTo(pEl);

        return r;
    }

    public String getDftValue() {
        return dftValue;
    }

    public void setDftValue(String dftValue) {
        this.dftValue = dftValue;
    }

}
