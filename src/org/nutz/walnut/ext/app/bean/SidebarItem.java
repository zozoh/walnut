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

    private String[] roles;

    public SidebarItem() {}

    public SidebarItem(WnObj o, SidebarItem siTmpl) {
        this.ph = o.path();
        this.icon = o.getString("icon", siTmpl.getIcon());
        if (!this.hasIcon())
            this.icon = String.format("<i class=\"oicon\" otp=\"%s\"></i>",
                                      Strings.sBlank(o.type(), "folder"));
        this.text = o.getString("title");
        if (!this.hasText())
            this.text = o.name();
        this.editor = o.getString("editor", siTmpl.getEditor());
        this.dynamic = true;
    }

    public SidebarItem setDefaultValue(SidebarItem siTmpl) {
        if (!this.hasIcon()) {
            this.setIcon(siTmpl.getIcon());
        }
        if (!this.hasEditor()) {
            this.setEditor(siTmpl.getEditor());
        }
        return this;
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

    public boolean hasEditor() {
        return !Strings.isBlank(this.editor);
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public boolean hasRoles() {
        return null != roles && roles.length > 0;
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
