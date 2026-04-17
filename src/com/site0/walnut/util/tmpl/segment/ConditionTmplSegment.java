package com.site0.walnut.util.tmpl.segment;

import com.site0.walnut.util.validate.WnMatch;

public class ConditionTmplSegment extends AbstractTmplSegment {

    private WnMatch match;

    public ConditionTmplSegment() {
        super();
    }

    public ConditionTmplSegment(WnMatch match, TmplSegment seg) {
        super();
        this.match = match;
        this.addChild(seg);
    }

    @Override
    public boolean isEnable(Object vars) {
        if (null == match) {
            return true;
        }
        return match.match(vars);
    }

    public WnMatch getMatch() {
        return match;
    }

    public void setMatch(WnMatch match) {
        this.match = match;
    }

}
