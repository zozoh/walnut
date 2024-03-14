package com.site0.walnut.ext.data.wf.bean;

import com.site0.walnut.ext.util.react.bean.ReactAction;

public abstract class WfActionElement {

    private ReactAction[] actions;

    public boolean hasActions() {
        return null != actions && actions.length > 0;
    }

    public ReactAction[] getActions() {
        return actions;
    }

    public void setActions(ReactAction[] actions) {
        this.actions = actions;
    }

}
