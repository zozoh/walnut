package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;

public class OEPlaceholder extends OEVarItem {

	private String dftValue;

	public OEPlaceholder() {
		this.type = OENodeType.PLACEHOLDER;
	}

	@Override
	public void renderTo(CheapElement pEl, NutBean vars) {
		Object v = vars.get(varName);
		String s = null == v ? dftValue : v.toString();
		CheapElement rPr = refer.clone();
		CheapDocument doc = pEl.getOwnerDocument();
		CheapElement r = doc.createElement("w:r");
		rPr.appendTo(r);
		CheapElement t = doc.createElement("w:t");
		t.setText(s);
		t.appendTo(r);
		r.appendTo(pEl);
	}

	public String getDftValue() {
		return dftValue;
	}

	public void setDftValue(String dftValue) {
		this.dftValue = dftValue;
	}

}
