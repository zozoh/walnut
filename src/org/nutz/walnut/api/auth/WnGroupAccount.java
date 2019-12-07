package org.nutz.walnut.api.auth;

import org.nutz.lang.util.NutMap;

public class WnGroupAccount {

    private String groupName;

    private WnAccount account;

    private WnGroupRole role;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public WnAccount getAccount() {
        return account;
    }

    public void setAccount(WnAccount user) {
        this.account = user;
    }

    public WnGroupRole getRole() {
        return role;
    }

    public void setRole(WnGroupRole role) {
        this.role = role;
    }

    public NutMap toBean() {
        NutMap bean = new NutMap();
        bean.put("grp", this.getGroupName());
        bean.put("uid", this.account.getId());
        bean.put("unm", this.account.getName());
        bean.put("role", this.role.getValue());
        bean.put("roleName", this.role.name());
        return bean;
    }

    public NutMap toBean(String... keys) {
        NutMap bean = this.toBean();
        return bean.pick(keys);
    }

}
