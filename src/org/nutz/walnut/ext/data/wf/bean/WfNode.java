package org.nutz.walnut.ext.data.wf.bean;

public class WfNode extends WfActionElement {

    private WfNodeType type;

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

}
