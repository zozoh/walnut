package org.nutz.walnut.ext.app.bean;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;

public class SidebarItem {

    private String type;

    private String cmd;

    private String ph;

    private String icon;

    private String text;

    private String editor;

    private boolean dynamic;

    public SidebarItem() {}

    public SidebarItem(WnObj o) {
        this.ph = o.path();
        this.icon = o.getString("icon");
        if (Strings.isBlank(this.icon))
            this.icon = String.format("<i class=\"oicon\" otp=\"%s\"></i>",
                                      Strings.sBlank(o.type(), "folder"));
        this.text = o.name();
        this.editor = o.getString("editor");
        this.dynamic = true;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isType(String type) {
        return null != this.type && this.type.equals(type);
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean hasIcon() {
        return !Strings.isBlank(this.icon);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean hasText() {
        return !Strings.isBlank(this.text);
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    void joinHtml(StringBuilder sb) {
        sb.append("\n    ");
        sb.append("<item ph=\"").append(Strings.escapeHtml(ph)).append('"');
        if (!Strings.isBlank(editor))
            sb.append(" editor=\"").append(Strings.escapeHtml(editor)).append('"');
        sb.append('>');
        if (!Strings.isBlank(icon))
            sb.append(icon);
        if (!Strings.isBlank(text))
            sb.append("<b>").append(Strings.escapeHtml(text)).append("</b>");
        sb.append("</item>");
    }

}
