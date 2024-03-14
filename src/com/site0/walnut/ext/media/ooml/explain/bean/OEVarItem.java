package com.site0.walnut.ext.media.ooml.explain.bean;

import com.site0.walnut.util.Ws;

public abstract class OEVarItem extends OEItem {

    protected String varName;

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = Ws.trim(varName).replaceAll("\\s+", "");
    }

    public String toBrief() {
        String brief = super.toBrief();
        if (!Ws.isBlank(varName)) {
            brief += " : $<" + varName + ">";
        }
        return brief;
    }
}
