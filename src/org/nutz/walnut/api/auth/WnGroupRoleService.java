package org.nutz.walnut.api.auth;

import java.util.List;

/**
 * 封装系统账组角色的的操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnGroupRoleService {

    /**
     * 获取某账户对象在指定的系统组中的权限
     * 
     * @param user
     *            账户对象
     * @param groupName
     *            系统的组名
     * @return 账户对象在指定组中的权限。默认为 <code>GUEST</code>
     */
    WnGroupRole getGroupRole(WnAccount user, String groupName);

    /**
     * 设置某账户在组中的权限
     * 
     * @param user
     *            账户对象
     * @param groupName
     *            系统的组名
     * @param role
     *            角色
     */
    void setGroupRole(WnAccount user, String groupName, WnGroupRole role);

    /**
     * 移除某账户在某组中的权限设定
     * 
     * @param user
     *            账户对象
     * @param groupName
     *            系统的组名
     * @return 账户在族中的角色权限。 null 表示未找到记录。
     */
    WnGroupRole removeGroupRole(WnAccount user, String groupName);

    /**
     * @param groupName
     *            系统的组名
     * @return 组内包括的所有账户，及其对应的权限
     */
    List<WnGroupAccount> getAccounts(String groupName);

    /**
     * @param user
     *            账户对象
     * @return 账户对象所在的所有组，以及对应权限
     */
    List<WnGroupAccount> getGroups(WnAccount user);

    /**
     * 判断某账户在给定的组中是否至少有一个符合指定的角色
     * 
     * @param role
     *            指定的角色
     * @param user
     *            账户对象
     * @param groupNames
     *            组名列表
     * @return 如果在任何一个给定组中是管理员返回 true，否则返回 false
     */
    boolean isRoleOfGroup(WnGroupRole role, WnAccount user, String... groupNames);

    /**
     * 判断某账户在给定的组中是否至少有一个是管理员
     * 
     * @param user
     *            账户对象
     * @param groupNames
     *            组名列表
     * @return 如果在任何一个给定组中是管理员返回 true，否则返回 false
     */
    boolean isAdminOfGroup(WnAccount user, String... groupNames);

    /**
     * 判断某账户在给定的组中是否至少有一个是成员
     * 
     * @param user
     *            账户对象
     * @param groupNames
     *            组名列表
     * @return 如果在任何一个给定组中是成员返回 true，否则返回 false
     */
    boolean isMemberOfGroup(WnAccount user, String... groupNames);

}
