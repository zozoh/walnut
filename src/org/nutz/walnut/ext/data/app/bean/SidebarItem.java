package org.nutz.walnut.ext.data.app.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;

public class SidebarItem {

    private String type;

    private String cmd;

    private String oid;

    private String ph;

    private String icon;

    private String defaultIcon;

    private String text;

    private String editor;

    private String defaultEditor;

    private boolean dynamic;

    private String[] roles;

    public SidebarItem() {}

    public SidebarItem(SidebarItem siTmpl) {
        this.oid = siTmpl.oid;
        this.ph = siTmpl.ph;
        this.icon = siTmpl.icon;
        this.defaultIcon = siTmpl.defaultIcon;
        this.text = siTmpl.text;
        this.editor = siTmpl.editor;
        this.defaultEditor = siTmpl.defaultEditor;
        this.dynamic = siTmpl.dynamic;
        this.roles = siTmpl.roles;
    }

    public SidebarItem updateBy(WnObj o) {
        this.oid = o.id();
        this.ph = o.path();
        // 图标
        if (!this.hasIcon()) {
            this.icon = o.getString("icon");
        }
        if (!this.hasIcon()) {
            this.icon = Strings.sBlank(this.defaultIcon,
                                       String.format("<i class=\"oicon\" otp=\"%s\"></i>",
                                                     Strings.sBlank(o.type(), "folder")));
        }
        // 文本
        if (!this.hasText()) {
            this.text = o.getString("title", o.name());
        }
        // 编辑器
        if (!this.hasEditor()) {
            this.editor = o.getString("editor", this.defaultEditor);
        }

        // 其他
        this.dynamic = true;

        // 返回以便链式赋值
        return this;
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

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
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

    public String getDefaultEditor() {
        return defaultEditor;
    }

    public void setDefaultEditor(String defaultEditor) {
        this.defaultEditor = defaultEditor;
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

    NutMap parseFontIcon(String val, NutMap dft) {
        if (Strings.isBlank(val))
            return dft;

        NutMap icon = Lang.map("className", "material-icons").setv("text", val);
        Matcher m = Pattern.compile("^([a-z]+)-(.+)$").matcher(val);
        if (m.find()) {
            // fontawsome
            if (m.group(1).matches("^fa[a-z]")) {
                icon.setv("className", m.group(1) + " fa-" + m.group(2));
                icon.setv("text", null);
            }
            // Other font libs
            else {
                icon.setv("className", m.group(1) + " " + val);
                icon.setv("text", null);
            }
        }
        return icon;
    }

    void joinHtml(StringBuilder sb) {
        sb.append("\n    ");
        sb.append("<item ph=\"").append(Strings.escapeHtml(ph)).append('"');
        if (!Strings.isBlank(editor))
            sb.append(" editor=\"").append(Strings.escapeHtml(editor)).append('"');
        if (!Strings.isBlank(oid))
            sb.append(" oid=\"").append(oid).append('"');
        sb.append('>');
        if (!Strings.isBlank(icon)) {
            // Icon 就是一个 HTML 片段
            if (Strings.isQuoteBy(icon, '<', '>')) {
                sb.append(icon);
            }
            // 兼容直接书写 icon class 的情况
            else {
                NutMap iconInfo = parseFontIcon(icon, new NutMap());
                String iconHtml = Tmpl.exec("<i class=\"${className}\"></i>", iconInfo);
                sb.append(iconHtml);
            }
        }
        if (!Strings.isBlank(text))
            sb.append("<a>").append(Strings.escapeHtml(text)).append("</a>");
        sb.append("</item>");
    }

}
