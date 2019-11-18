package org.nutz.walnut.api.auth;

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
}
