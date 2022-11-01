package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Wlang;

public class OEBranch extends OENode {

    public OEBranch() {
        this.type = OENodeType.BRANCH;
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        List<OECondition> brs = this.getBranches();
        for (OECondition br : brs) {
            if (br.isMatch(vars)) {
                br.renderTo(pEl, vars);
                break;
            }
        }
        return pEl;
    }

    public boolean hasBranches() {
        return this.hasChildren();
    }

    public void addBranch(OECondition cond) {
        this.addChild(cond);
    }

    public List<OECondition> getBranches() {
        if (!this.hasChildren()) {
            return new LinkedList<>();
        }
        List<OECondition> brs = new ArrayList<>(children.size());
        for (OEItem it : this.children) {
            if (it instanceof OECondition) {
                brs.add((OECondition) it);
            }
        }
        return brs;
    }

    public void setBranches(List<OECondition> branches) {
        this.clearChildren();
        for (OECondition cond : branches) {
            this.addChild(cond);
        }
    }

    @Override
    public void addChild(OEItem node) {
        if (node instanceof OECondition) {
            super.addChild(node);
        } else {
            throw Er.create("e.ooml.tmpl.InvalidBranchChild", node.toBrief());
        }
    }

    @Override
    public void setChildren(List<OEItem> children) {
        throw Wlang.noImplement();
    }

}
