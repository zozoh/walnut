package com.site0.walnut.ext.data.titanium.util;

import java.util.List;

import org.nutz.lang.util.NutMap;

public class TiMetas {

    private int currentTab = 0;

    private List<NutMap> fields;

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int currentTab) {
        this.currentTab = currentTab;
    }

    public List<NutMap> getFields() {
        return fields;
    }

    public void setFields(List<NutMap> fields) {
        this.fields = fields;
    }

}
