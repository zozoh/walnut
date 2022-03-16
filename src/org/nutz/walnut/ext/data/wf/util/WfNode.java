package org.nutz.walnut.ext.data.wf.util;

import org.nutz.walnut.ext.util.react.bean.ReactAction;

public class WfNode {

    private WfNodeType type;

    private ReactAction[] actions;

    public boolean isHEAD() {
        return WfNodeType.HEAD == type;
    }

    public boolean isSTATE() {
        return WfNodeType.STATE == type;
    }

    public boolean isACITON() {
        return WfNodeType.ACITON == type;
    }

    public boolean isTAIL() {
        return WfNodeType.TAIL == type;
    }

    public WfNodeType getType() {
        return type;
    }

    public void setType(WfNodeType type) {
        this.type = type;
    }

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
