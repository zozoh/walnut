package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Wlang;

public class OEBranch extends OENode {

    private List<OECondition> branches;

    public OEBranch() {
        this.type = OENodeType.BRANCH;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        if (this.hasBranches()) {
            for (OECondition bra : branches) {
                if (bra.isMatch(vars)) {
                    bra.renderTo(pEl, vars);
                    break;
                }
            }
        }
        return pEl;
    }

    public boolean hasBranches() {
        return null != branches && !branches.isEmpty();
    }

    public void addBranch(OECondition cond) {
        if (null == branches) {
            this.branches = new LinkedList<>();
        }
        this.branches.add(cond);
        cond.setParent(this);
    }

    public List<OECondition> getBranches() {
        return branches;
    }

    public void setBranches(List<OECondition> branches) {
        this.branches = branches;
    }

    @Override
    public void addChild(OEItem node) {
        throw Wlang.noImplement();
    }

    @Override
    public void setChildren(List<OEItem> children) {
        throw Wlang.noImplement();
    }

}
