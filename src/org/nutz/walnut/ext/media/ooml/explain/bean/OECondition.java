package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AlwaysMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class OECondition extends OENode {

    private WnMatch match;

    public OECondition() {
        this.type = OENodeType.CONDITION;
    }

    public String toBrief() {
        String s = super.toBrief();
        return String.format("%s : Match: `%s`", s, match);
    }

    @Override
    public CheapElement renderTo(CheapElement pEl, NutBean vars) {
        if (this.hasChildren()) {
            for (OEItem child : this.children) {
                child.renderTo(pEl, vars);
            }
        }
        return pEl;
    }

    public boolean isMatch(NutBean vars) {
        return match.match(vars);
    }

    public void setAsDefaultBranch() {
        this.match = new AlwaysMatch(true);
    }

    public void setMatch(String match) {
        String s = match.replaceAll("[“”]", "\"");
        Object input;
        if (Ws.isQuoteBy(s, '[', ']')) {
            input = Json.fromJson(s);
        } else {
            input = Wlang.map(s);
        }
        this.match = AutoMatch.parse(input, false);
    }

    public WnMatch getMatch() {
        return match;
    }

    public void setMatch(WnMatch match) {
        this.match = match;
    }
}