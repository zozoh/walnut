package org.nutz.walnut.ext.data.titanium.sidebar;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

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

    private NutMap pvg;

    public String toString() {
        String s = "";
        if (null != key) {
            s += ":" + key;
        }
        if (null != title) {
            s += ":" + title;
        }
        if (null != path) {
            s += ":" + path;
        }
        JsonFormat jfmt = JsonFormat.compact();
        if (this.hasRoles()) {
            s += ":" + Json.toJson(roles, jfmt);
        }
        if (this.hasPvg()) {
            s += ":PVG" + Json.toJson(pvg, jfmt);
        }
        if (null != items && items.length > 0) {
            for (TiSidebarInputItem it : items) {
                s += "\n - " + it.toString();
            }
        }
        return s;
    }

    public TiSidebarInputItem clone() {
        TiSidebarInputItem it2 = new TiSidebarInputItem();
        if (null != this.items) {
            it2.items = new TiSidebarInputItem[this.items.length];
            for (int i = 0; i < it2.items.length; i++) {
                it2.items[i] = this.items[i].clone();
            }
        }
        it2.command = this.command;
        it2.key = this.key;
        it2.path = this.path;
        it2.icon = this.icon;
        it2.title = this.title;
        it2.view = this.view;
        it2.defaultIcon = this.defaultIcon;
        it2.defaultTitle = this.defaultTitle;
        it2.defaultView = this.defaultView;
        if (null != roles) {
            it2.roles = new HashMap<>();
            it2.roles.putAll(this.roles);
        }
        if (this.hasPvg()) {
            it2.pvg = new NutMap();
            it2.pvg.putAll(this.pvg);
        }
        return it2;
    }

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
        return null != roles && !roles.isEmpty();
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }

    public boolean hasPvg() {
        return null != pvg && !pvg.isEmpty();
    }

    public NutMap getPvg() {
        return pvg;
    }

    public void setPvg(NutMap pvg) {
        this.pvg = pvg;
    }

}
