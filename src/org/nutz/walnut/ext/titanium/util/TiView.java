package org.nutz.walnut.ext.titanium.util;

import org.nutz.lang.util.NutMap;

public class TiView {

    private String comIcon;

    private String comType;

    private NutMap comConf;

    private String modType;

    private String[] components;

    private Object actions;

    public String getComIcon() {
        return comIcon;
    }

    public void setComIcon(String comIcon) {
        this.comIcon = comIcon;
    }

    public String getComType() {
        return comType;
    }

    public void setComType(String comType) {
        this.comType = comType;
    }

    public NutMap getComConf() {
        return comConf;
    }

    public void setComConf(NutMap comConf) {
        this.comConf = comConf;
    }

    public String getModType() {
        return modType;
    }

    public void setModType(String modType) {
        this.modType = modType;
    }

    public String[] getComponents() {
        return components;
    }

    public void setComponents(String[] components) {
        this.components = components;
    }

    public Object getActions() {
        return actions;
    }

    public void setActions(Object actions) {
        this.actions = actions;
    }

}
