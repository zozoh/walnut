package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OEBranch extends OEItem {

    private List<OECondition> branches;

    public OEBranch() {
        this.type = OENodeType.BRANCH;
    }

    @Override
    public void renderTo(CheapElement pEl, NutBean vars) {
        if (this.hasBranches()) {
            for (OECondition bra : branches) {
                if (bra.isMatch(vars)) {
                    bra.renderTo(pEl, vars);
                    break;
                }
            }
        }
    }

    public boolean hasBranches() {
        return null != branches && !branches.isEmpty();
    }

    public void addBranch(OECondition condition) {
        if (null == branches) {
            this.branches = new LinkedList<>();
        }
        this.branches.add(condition);
    }

    public List<OECondition> getBranches() {
        return branches;
    }

    public void setBranches(List<OECondition> branches) {
        this.branches = branches;
    }

}
