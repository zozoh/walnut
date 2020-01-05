package org.nutz.walnut.ext.titanium.sidebar;

import java.util.Map;

import org.nutz.lang.Strings;

public class TiSidebarInputItem {

    private TiSidebarInputItem[] items;

    private String command;

    private String key;

    private String path;

    private String icon;

    private String title;

    private String view;

    private String defaultIcon;

    private String defaultTitle;

    private String defaultView;

    private Map<String, String> roles;

    public boolean isGroup() {
        return null != items && items.length > 0;
    }

    public boolean hasCommand() {
        return !Strings.isBlank(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean hasItems() {
        return null != this.items && items.length > 0;
    }

    public TiSidebarInputItem[] getItems() {
        return items;
    }

    public void setItems(TiSidebarInputItem[] items) {
        this.items = items;
    }

    public boolean hasKey() {
        return !Strings.isBlank(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean hasPath() {
        return !Strings.isBlank(this.path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultText) {
        this.defaultTitle = defaultText;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }

    public boolean hasRoles() {
        return null != roles && roles.size() > 0;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }

}
