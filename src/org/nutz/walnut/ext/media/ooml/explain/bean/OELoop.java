package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OELoop extends OENode {

	private String subName;

	public OELoop() {
		this.type = OENodeType.LOOP;
	}

	@Override
	public void renderTo(CheapElement pEl, NutBean vars) {
		Object list = vars.get(varName);
		// 防空
		if (null == list || !hasChildren()) {
			return;
		}

		if (list instanceof List<?>) {
			Object old = vars.get(subName);
			for (Object li : (List<?>) list) {
				vars.put(subName, li);
				for (OEItem it : children) {
					it.renderTo(pEl, vars);
				}
			}
			vars.put(subName, old);
		}
	}

}
