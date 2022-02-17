package org.nutz.walnut.api.auth;

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
