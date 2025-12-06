package com.site0.walnut.login.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

public class WnSimpleRole implements WnRole {

    private String id;

    private String userId;

    private String userName;

    /**
     * 用户角色的类型，管理员/组员/访客/黑名单
     */
    private WnRoleType type;

    /**
     * 用户角色的组名称
     */
    private String group;

    /**
     * 用户特殊权限说明
     */
    private Map<String, Boolean> privileges;

    String privilegesToString() {
        if (null == privileges || privileges.isEmpty()) {
            return null;
        }
        List<String> keys = new ArrayList<>(this.privileges.size());
        for (Map.Entry<String, Boolean> en : this.privileges.entrySet()) {
            Boolean yes = en.getValue();
            if (null != yes && yes) {
                keys.add(en.getKey());
            }
        }
        return Ws.join(keys, ",");
    }

    void privilegesFromString(String input) {
        if (Ws.isBlank(input)) {
            this.privileges = null;
        } else {
            this.privileges = new HashMap<>();
            String[] keys = Ws.splitIgnoreBlank(input);
            for (String key : keys) {
                this.privileges.put(key, true);
            }
        }

    }

    @Override
    public void fromBean(NutBean bean) {
        WnRoleType type = WnRoles.fromInt(bean.getInt("role"));
        this.setId(bean.getString("id"));
        this.setType(type);
        this.setUserId(bean.getString("uid"));
        this.setUserName(bean.getString("unm"));
        this.setGroup(bean.getString("grp"));
        this.privilegesFromString(bean.getString("privileges"));
    }

    @Override
    public NutBean toBean() {
        NutMap bean = new NutMap();
        bean.put("id", id);
        bean.put("grp", this.group);
        bean.put("uid", this.userId);
        bean.put("unm", this.userName);
        bean.put("type", type.toString());
        bean.put("role", type.getValue());
        bean.put("privileges", this.privilegesToString());
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
    public boolean isMatchGroup(String grp) {
        if (null == grp) {
            return false;
        }
        return grp.equalsIgnoreCase(this.group);
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
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
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public void mergePrivilegesTo(Map<String, Boolean> map) {
        if (!this.hasPrivileges())
            return;
        for (Map.Entry<String, Boolean> en : this.privileges.entrySet()) {
            Boolean yes = en.getValue();
            if (null != yes && yes) {
                map.put(en.getKey(), true);
            }
        }
    }

    @Override
    public boolean hasPrivileges() {
        return null != privileges && !privileges.isEmpty();
    }

    @Override
    public Map<String, Boolean> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Map<String, Boolean> privileges) {
        this.privileges = privileges;
    }
}
