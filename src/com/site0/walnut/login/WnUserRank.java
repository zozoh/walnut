package com.site0.walnut.login;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.core.bean.WnObjMode;

/**
 * 根据用户对象，获取一个用户的
 * <ol>
 * <li><code>ID</code> 用户 ID
 * <li><code>name</code> 用户登录名
 * <li><code>roles</code> 系统分配的角色
 * </ol>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnUserRank {

    private String userId;

    private String userName;

    private WnRoleList roles;

    /**
     * 根据自定权限设定集合，评判当前用户(UserRang) 的权限吗
     * 
     * @param pvg
     *            权限设定集合
     * @param dftMode
     *            默认权限码
     * @return 用户在权限设定集合所具备的最高权限码
     */
    public int evalPvgMode(NutBean pvg, int dftMode) {
        // 防空
        if (null == pvg || pvg.isEmpty()) {
            return dftMode;
        }
        // 准备权限
        boolean found = false;
        int md = 0;
        String key;
        Object val;
        //
        // 处理各种自定义的权限
        //
        // - 4a98..a123 : 直接是用户的 ID
        key = this.userId;
        val = pvg.get(key);
        if (null != val) {
            found = true;
            WnObjMode wom = WnObjMode.parse(val);
            md |= wom.getValue();
            // 嗯，511(777)，到头了，返回
            if (md >= 511) {
                return 511;
            }
        }
        // - @[demo] : 角色【域账户】
        key = "@[" + this.userName + "]";
        val = pvg.get(key);
        if (null != val) {
            found = true;
            WnObjMode wom = WnObjMode.parse(val);
            md |= wom.getValue();
            // 嗯，511(777)，到头了，返回
            if (md >= 511) {
                return 511;
            }
        }
        // - @others : 角色【域账户】
        val = pvg.get("@others");
        if (null != val) {
            found = true;
            WnObjMode wom = WnObjMode.parse(val);
            md |= wom.getValue();
            // 嗯，511(777)，到头了，返回
            if (md >= 511) {
                return 511;
            }
        }
        // - @SYS_ADMIN : 角色【域账户】
        if (!roles.isEmpty()) {
            for (WnRole r: roles) {
                key = "@" + r.getName();
                val = pvg.get(key);
                if (null != val) {
                    found = true;
                    WnObjMode wom = WnObjMode.parse(val);
                    md |= wom.getValue();
                    if (md >= 511) {
                        return 511;
                    }
                }
            }
        }
        // 如果已经获得了属性，那么就直接返回
        if (found) {
            return md;
        }

        // 那就是没有啊
        return md > 0 ? md : dftMode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public WnRoleList getRoles() {
        return roles;
    }

    public void setRoles(WnRoleList roles) {
        this.roles = roles;
    }

}
