package org.nutz.walnut.cheap.dom;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Ws;

public class CheapDocType extends CheapNode {

    /**
     * 如果是 HTML的话，串行化，应该是 <code><!DOCTYPE html></code>
     * <p>
     * 否则就是 xml,串行化，应该是
     * 
     * <pre>
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     * </pre>
     * 
     * 那么 <code>props</code> 一般是version, encoding, standalone 这三个
     */
    private boolean html;

    public CheapDocType() {
        super();
        this.type = CheapNodeType.DOC_TYPE;
    }

    public CheapDocType(boolean asHtml) {
        this();
        this.html = asHtml;
    }

    public CheapDocType(NutBean attrs) {
        this();
        this.html = false;
        this.propsPutAll(attrs);
    }

    @Override
    public CheapDocType cloneNode() {
        CheapDocType re = new CheapDocType();
        re.html = this.html;
        if (null != this.props) {
            re.props.putAll(this.props);
        }
        return re;
    }

    @Override
    public void decodeEntities() {}

    @Override
    public String toBrief() {
        return this.toString();
    }

    @Override
    public void joinTree(StringBuilder sb, int depth, String tab) {
        sb.append(Ws.repeat(tab, depth));
        sb.append("|-- ");
        sb.append(this.toBrief());
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append(this.toString());
    }

    @Override
    public void joinText(StringBuilder sb) {}

    @Override
    public void compact() {}

    @Override
    public void compactWith(CheapNodeFilter flt) {}

    @Override
    public void setText(String text) {}

    @Override
    public void format(CheapFormatter cdf, int depth) {}

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isBlank() {
        return true;
    }

    @Override
    public String toString() {
        if (html) {
            return "<!DOCTYPE html>";
        }
        // 那么就是 xml 咯
        if (null == props || props.isEmpty()) {
            return "<?xml version=\"1.0\"?>";
        }
        StringBuilder sb = new StringBuilder("<?xml");
        for (String name : props.keySet()) {
            String val = props.getString(name);
            sb.append(' ').append(name).append("=\"").append(val).append("\"");
        }
        sb.append("?>");
        return sb.toString();
    }

    public boolean isHtml() {
        return html;
    }

    public boolean isXml() {
        return !html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

}
