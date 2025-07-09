package com.site0.walnut.login.role;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.WnRole;
import com.site0.walnut.login.WnRoleType;
import com.site0.walnut.util.Ws;

public class WnSimpleRole implements WnRole {

    private String userId;

    private String userName;

    private WnRoleType type;

    private String name;

    @Override
    public NutBean toBean() {
        NutMap bean = new NutMap();
        bean.put("grp", this.name);
        bean.put("uid", this.userId);
        bean.put("usr", this.userName);
        bean.put("type", type.toString());
        bean.put("role", type.getValue());
        return bean;
    }

    @Override
    public boolean isAdmin() {
        return WnRoleType.ADMIN == this.type;
    }

    @Override
    public boolean isMember() {
        return WnRoleType.MEMBER == this.type;
    }

    @Override
    public boolean isMatchName(String name) {
        if (null == name) {
            return false;
        }
        return name.equalsIgnoreCase(this.name);
    }

    @Override
    public boolean hasUserId() {
        return !Ws.isBlank(userId);
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean hasUserName() {
        return !Ws.isBlank(userName);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public WnRoleType getType() {
        return type;
    }

    public void setType(WnRoleType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
