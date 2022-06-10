package org.nutz.walnut.ext.data.wf.bean;

import org.nutz.walnut.ext.util.react.bean.ReactAction;

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
