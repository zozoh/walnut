package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.walnut.util.Ws;

public abstract class OEVarItem extends OEItem {

    protected String varName;

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String toBrief() {
        String brief = super.toBrief();
        if (!Ws.isBlank(varName)) {
            brief += " : $<" + varName + ">";
        }
        return brief;
    }
}
