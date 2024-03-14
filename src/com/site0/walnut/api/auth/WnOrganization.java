package com.site0.walnut.api.auth;

import java.util.Collection;

import com.site0.walnut.util.validate.WnMatch;

/**
 * 组织结构节点
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOrganization {

    private String id;

    private String type;

    private String icon;

    private String name;

    private String[] roleActions;

    private WnOrganization[] children;

    public void joinRoleActions(Collection<String> col, WnMatch test) {
        // 防守
        if (null == test) {
            return;
        }
        // 添加自己
        if (this.hasRoleActions() && test.match(id)) {
            for (String ra : roleActions) {
                col.add(ra);
            }
        }
        // 添加子节点
        if (this.hasChildren()) {
            for (WnOrganization child : children) {
                child.joinRoleActions(col, test);
            }
        }
    }

    public String toString() {
        return String.format("[%s] %s: %s", type, id, name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasRoleActions() {
        return null != roleActions && roleActions.length > 0;
    }

    public String[] getRoleActions() {
        return roleActions;
    }

    public void setRoleActions(String[] roleActions) {
        this.roleActions = roleActions;
    }

    public boolean hasChildren() {
        return null != children && children.length > 0;
    }

    public WnOrganization[] getChildren() {
        return children;
    }

    public void setChildren(WnOrganization[] children) {
        this.children = children;
    }

}
