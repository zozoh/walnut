package org.nutz.walnut.ext.data.titanium.util;

import org.nutz.lang.util.NutMap;

public class TiView {

    private String comIcon;

    private String comType;

    private Object comConf;

    private String modType;

    private NutMap events;

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

    public Object getComConf() {
        return comConf;
    }

    public void setComConf(Object comConf) {
        this.comConf = comConf;
    }

    public String getModType() {
        return modType;
    }

    public void setModType(String modType) {
        this.modType = modType;
    }

    public NutMap getEvents() {
        return events;
    }

    public void setEvents(NutMap events) {
        this.events = events;
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