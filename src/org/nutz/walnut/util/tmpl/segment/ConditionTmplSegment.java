package org.nutz.walnut.util.tmpl.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.validate.WnMatch;

public class ConditionTmplSegment extends AbstractTmplSegment {

    private WnMatch match;

    public ConditionTmplSegment() {
        super();
    }

    public ConditionTmplSegment(WnMatch match, TmplSegment seg) {
        super();
        this.match = match;
        this.addSegment(seg);
    }

    @Override
    public boolean isEnable(NutBean vars) {
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
