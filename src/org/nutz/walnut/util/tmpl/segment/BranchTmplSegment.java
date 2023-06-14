package org.nutz.walnut.util.tmpl.segment;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.ele.TmplEle;
import org.nutz.walnut.util.validate.WnMatch;

public class BranchTmplSegment extends AbstractTmplSegment {

    @Override
    public void renderTo(NutBean context, boolean showKey, StringBuilder sb) {
        if (null != children) {
            for (TmplSegment seg : children) {
                if (seg.isEnable(context)) {
                    seg.renderTo(context, showKey, sb);
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
