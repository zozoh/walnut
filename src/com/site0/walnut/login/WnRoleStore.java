package com.site0.walnut.login;

public interface WnRoleStore {

    WnRoleList getRoles(WnUser u);

    /**
     * 获取所有符合指定名称的角色列表， 也就是说，给定一个角色名称，本函数会返回所有相关的用户ID
     * 
     * @param name
     *            角色名称
     * @return 角色列表
     */
    WnRoleList queryRolesOf(String name);

    void removeRole(WnRole role);

    /**
     * 移除用户某个指定角色
     * 
     * @param uid
     *            用户ID
     * @param name
     *            角色名
     */
    void removeRole(String uid, String name);

    void clearCache();

    /**
     * 根据用户 ID 获取所有相关的角色设置
     * 
     * @param uid
     *            用户 ID
     * @return 角色列表
     */
    WnRoleList getRoles(String uid);

    /**
     * 为某个用户在某个组创建一个橘色
     * 
     * @param uid
     *            用户 ID
     * @param name
     *            角色组名
     * @param type
     *            角色类型
     * @return 新创建的用户角色信息
     */
    WnRole addRole(String uid, String name, WnRoleType type);

}
