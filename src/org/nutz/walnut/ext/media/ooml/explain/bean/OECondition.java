package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AlwaysMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class OECondition extends OENode {

	private WnMatch match;

	public OECondition() {
		this.type = OENodeType.CONDITION;
	}

	@Override
	public void renderTo(CheapElement pEl, NutBean vars) {
		if (this.hasChildren()) {
			for (OEItem child : this.children) {
				child.renderTo(pEl, vars);
			}
		}
	}

	public boolean isMatch(NutBean vars) {
		Object v = getMatchVar(vars);
		return match.match(v);
	}

	private Object getMatchVar(NutBean vars) {
		if (Ws.isBlank(varName)) {
			return vars;
		}
		return vars.get(varName);
	}

	public void setAsDefaultBranch() {
		this.match = new AlwaysMatch(true);
	}

	public void setMatch(String match) {
		this.match = AutoMatch.parse(match, false);
	}

	public WnMatch getMatch() {
		return match;
	}

	public void setMatch(WnMatch match) {
		this.match = match;
	}
}
