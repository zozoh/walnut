package org.nutz.walnut.ext.media.ooml.explain.bean;

import java.util.LinkedList;
import java.util.List;

public abstract class OENode extends OEVarItem {

	protected List<OEItem> children;

	public void joinTrace(StringBuilder sb, int depth) {
		this.joinTrace(sb, depth);
		if (this.hasChildren()) {
			for (OEItem it : children) {
				sb.append("\n");
				it.joinTrace(sb, depth + 1);
			}
		}
	}

	public boolean hasChildren() {
		return null != children && !children.isEmpty();
	}

	public void addChild(OEItem node) {
		if (null == children) {
			children = new LinkedList<>();
		}
		node.setOoml(ooml);
		node.setLoader(loader);
		node.setEntry(entry);
		children.add(node);
	}

	public List<OEItem> getChildren() {
		return children;
	}

	public void setChildren(List<OEItem> children) {
		this.children = children;
	}

}
