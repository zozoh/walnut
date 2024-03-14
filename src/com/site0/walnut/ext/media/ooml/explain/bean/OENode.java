package com.site0.walnut.ext.media.ooml.explain.bean;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.cheap.api.CheapResourceLoader;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.ooml.OomlPackage;

public abstract class OENode extends OEItem {

    protected List<OEItem> children;

    public void joinTrace(StringBuilder sb, int depth) {
        super.joinTrace(sb, depth);
        if (this.hasChildren()) {
            for (OEItem it : children) {
                sb.append("\n");
                it.joinTrace(sb, depth + 1);
            }
        }
    }

    @Override
    public void setOoml(OomlPackage ooml) {
        super.setOoml(ooml);
        if (this.hasChildren()) {
            for (OEItem child : this.children) {
                child.setOoml(ooml);
            }
        }
    }

    @Override
    public void setLoader(CheapResourceLoader loader) {
        super.setLoader(loader);
        if (this.hasChildren()) {
            for (OEItem child : this.children) {
                child.setLoader(loader);
            }
        }
    }

    @Override
    public void setEntry(OomlEntry entry) {
        super.setEntry(entry);
        if (this.hasChildren()) {
            for (OEItem child : this.children) {
                child.setEntry(entry);
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
        node.setParent(this);
        children.add(node);
    }

    public void clearChildren() {
        if (null != children) {
            children.clear();
        }
    }

    public List<OEItem> getChildren() {
        return children;
    }

    public void setChildren(List<OEItem> children) {
        this.children = children;
    }

}
