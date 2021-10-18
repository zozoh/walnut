package org.nutz.walnut.api.auth;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.util.Ws;

public enum WnGroupRole {
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
     * 预备组员，等待管理员审批，期间，和非组员权限一样
     */
    CANDIDATE(100),

    /**
     * 黑名单，完全阻止任何访问，也不可以自我申请为预备组员
     */
    BLOCK(-1);

    private int value;

    private WnGroupRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    /**
     * 是否比给定的角色权限更高
     * 
     * @return 如果比给定的权限更高，返回 true
     */
    public boolean isHigherThen(WnGroupRole role) {
        if (null == role) {
            return true;
        }
        if (this.value == role.value) {
            return false;
        }
        // 当前如果是 GUEST，只有 管理员/组员/预备组员都比自己高
        if (this.value == 0) {
            return role.value > 0;
        }
        // 如果当前是 ADMIN，没有权限比它高
        if (this.value == 1) {
            return false;
        }
        // 如果当前是 MEMBER，只有管理员比自己高
        if (this.value == 10) {
            return role.value == 1;
        }
        // 如果当前是 CANDIDATE，管理员和组员都比自己高
        if (this.value == 100) {
            return role.value == 1 || role.value == 10;
        }
        // 如果当前是黑名单，所有身份都比它高
        if (this.value < 0) {
            return role.value > 0;
        }
        // 这种情况不可能，总之返回 false 吧
        return false;
    }

    public static WnGroupRole parseAny(Object role, WnGroupRole dft) {
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

    public static WnGroupRole parseAny(Object role) {
        return parseAny(role, GUEST);
    }

    public static WnGroupRole parseInt(int role) {
        if (0 == role)
            return GUEST;
        if (1 == role)
            return ADMIN;
        if (10 == role)
            return MEMBER;
        if (100 == role)
            return CANDIDATE;
        if (-1 == role)
            return BLOCK;
        return GUEST;
    }

    private static Map<String, WnGroupRole> roles = new HashMap<>();
    static {
        roles.put("GUEST", GUEST);
        roles.put("ADMIN", ADMIN);
        roles.put("MEMBER", MEMBER);
        roles.put("CANDIDATE", CANDIDATE);
        roles.put("BLOCK", BLOCK);
    }

    public static WnGroupRole parseString(String s) {
        if (null == s) {
            return GUEST;
        }
        s = Ws.trim(s).toUpperCase();
        WnGroupRole r = roles.get(s);
        if (null == r) {
            return GUEST;
        }
        return r;
    }
}
