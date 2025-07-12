package com.site0.walnut.login.role;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.util.Ws;

public enum WnRoleType {

    /**
     * （默认）非组员，不在组内
     */
    GUEST(0),

    /**
     * 管理员，拥有最高权限
     */
    ADMIN(1),

    /**
     * 组员
     */
    MEMBER(10),

    /**
     * 黑名单，完全阻止任何访问，也不可以自我申请为预备组员
     */
    BLOCK(-1);

    private int value;

    private WnRoleType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public boolean isOf(WnRoleType role) {
        if (null == role) {
            return false;
        }
        if (this.value == role.getValue()) {
            return true;
        }
        // 自己在黑名单里，那么对方只要不是在黑名单里就不等
        // 自己是后补，对方只要不是候补就不等
        if (this.value < 0 || this.value >= 100) {
            return false;
        }
        // 自己是成员
        if (MEMBER == this) {
            return role == GUEST || role == MEMBER;
        }
        // 自己是管理员，对方只要是成员或者管理员都可以
        if (ADMIN == this) {
            return role == ADMIN || role == MEMBER || role == GUEST;
        }
        // 自己是管理员，只有对方也是管理员才可以
        // 但是前面已经判断了不等，这里就都返回 false 了
        return false;
    }

    /**
     * 是否比给定的角色权限更高
     * 
     * @return 如果比给定的权限更高，返回 true
     */
    public boolean isHigherThen(WnRoleType role) {
        if (null == role) {
            return true;
        }
        if (this.value == role.value) {
            return false;
        }
        // 当前如果是 GUEST，只比黑名单高
        if (this.value == 0) {
            return role.value < 0;
        }
        // 如果当前是 ADMIN，没有权限比它高
        if (this.value == 1) {
            return role.value != 1;
        }
        // 如果当前是 MEMBER，比预备和黑名单高
        if (this.value == 10) {
            return role.value >= 100 || role.value <= 0;
        }
        // 如果当前是 CANDIDATE，比访客和黑名单高
        if (this.value == 100) {
            return role.value <= 0;
        }
        // 如果当前是黑名单，所有身份都比它高
        if (this.value < 0) {
            return true;
        }
        // 这种情况不可能，总之返回 false 吧
        return false;
    }

    public static WnRoleType parseAny(Object role, WnRoleType dft) {
        if (null == role) {
            return dft;
        }
        if (role instanceof Integer) {
            int v = (Integer) role;
            return parseInt(v);
        }
        String s = role.toString();
        return parseString(s);
    }

    public static WnRoleType parseAny(Object role) {
        return parseAny(role, GUEST);
    }

    public static WnRoleType parseInt(int role) {
        if (0 == role)
            return GUEST;
        if (1 == role)
            return ADMIN;
        if (10 == role)
            return MEMBER;
        if (-1 == role)
            return BLOCK;
        return GUEST;
    }

    private static Map<String, WnRoleType> roles = new HashMap<>();
    static {
        roles.put("GUEST", GUEST);
        roles.put("ADMIN", ADMIN);
        roles.put("MEMBER", MEMBER);
        roles.put("BLOCK", BLOCK);
    }

    public static WnRoleType parseString(String s) {
        if (null == s) {
            return GUEST;
        }
        s = Ws.trim(s).toUpperCase();
        WnRoleType r = roles.get(s);
        if (null == r) {
            return GUEST;
        }
        return r;
    }

}
