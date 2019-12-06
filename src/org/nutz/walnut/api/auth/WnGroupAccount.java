package org.nutz.walnut.api.auth;

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

}
