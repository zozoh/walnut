package org.nutz.walnut.util.tmpl.segment;

import java.util.List;

import org.nutz.walnut.util.tmpl.WnTmplRenderContext;
import org.nutz.walnut.util.tmpl.ele.TmplEle;
import org.nutz.walnut.util.validate.WnMatch;

public class BranchTmplSegment extends AbstractTmplSegment {

    @Override
    public void renderTo(WnTmplRenderContext rc) {
        if (null != children) {
            for (TmplSegment seg : children) {
                if (seg.isEnable(rc.context)) {
                    seg.renderTo(rc);
                    break;
                }
            }
        }
    }

    public void addCondition(WnMatch match, List<TmplEle> elements) {
        BlockTmplSegment block = new BlockTmplSegment(elements);
        ConditionTmplSegment cnd = new ConditionTmplSegment(match, block);
        this.addChild(cnd);
    }
}
