package org.nutz.walnut.cheap.dom.mutation;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;
import org.nutz.walnut.util.Ws;

public class CheapDomMutation implements DomMutation {

    private String tagName;

    private String className;

    private String addClass;

    private NutMap attrs;

    private NutMap updateAttrs;

    private String html;

    private String text;

    private String wrap;

    private boolean unwrap;

    @Override
    public List<CheapElement> mutate(CheapElement el) {
        List<CheapElement> re = new LinkedList<>();
        re.add(el);

        if (!Ws.isBlank(tagName)) {
            el.setTagName(tagName);
        }
        if (!Ws.isBlank(className)) {
            el.setClassName(className);
        }
        if (!Ws.isBlank(addClass)) {
            el.addClass(addClass);
        }
        if (null != attrs) {
            el.setAttrs(attrs);
        }
        if (null != updateAttrs) {
            el.attrs(updateAttrs);
        }
        if (!Ws.isBlank(html)) {
            el.setInnerXML(html);
        }
        if (!Ws.isBlank(text)) {
            el.setText(text);
        }
        if (!Ws.isBlank(wrap)) {
            CheapDocument doc = el.getOwnerDocument();
            CheapElement newEl = doc.createElement(wrap);
            newEl.setChildren(el.getChildren());
            el.insertPrev(newEl);
            el.remove();
            el = newEl;
            re.clear();
            re.add(el);
        }
        if (unwrap) {
            List<CheapNode> nodes = el.getChildren();
            el.insertPrevNodes(nodes);
            el.remove();
            re.clear();
            for (CheapNode node : nodes) {
                if (node.isElement()) {
                    re.add((CheapElement) node);
                }
            }
        }

        return re;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAddClass() {
        return addClass;
    }

    public void setAddClass(String addClass) {
        this.addClass = addClass;
    }

    public NutMap getAttrs() {
        return attrs;
    }

    public void setAttrs(NutMap attrs) {
        this.attrs = attrs;
    }

    public NutMap getUpdateAttrs() {
        return updateAttrs;
    }

    public void setUpdateAttrs(NutMap updateAttrs) {
        this.updateAttrs = updateAttrs;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getWrap() {
        return wrap;
    }

    public void setWrap(String wrap) {
        this.wrap = wrap;
    }

    public boolean isUnwrap() {
        return unwrap;
    }

    public void setUnwrap(boolean unwrap) {
        this.unwrap = unwrap;
    }

}
