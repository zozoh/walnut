package org.nutz.walnut.api.usr;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnQuery;

public interface WnUsrService {

    /**
     * 创建一个用户，同时为其创建 /home 和 /sys/grp 下的内容
     * 
     * @param info
     *            用户信息
     * @return 用户对象
     */
    WnUsr create(WnUsrInfo info);

    /**
     * 一个快捷的便于阅读的登录名。 这个操作同时会修改用户主组以及 /home/UNM 两个目录的名称
     * 
     * @param u
     *            用户
     * @param newName
     *            新的登录名
     */
    void rename(WnUsr u, String newName);

    /**
     * @param nm
     *            用户登录名
     * @param passwd
     *            密码
     * @return 密码是否正确
     */
    boolean checkPassword(String nm, String passwd);

    /**
     * @param u
     *            用户
     * @param passwd
     *            密码
     * @return 密码是否正确
     */
    boolean checkPassword(WnUsr u, String passwd);

    /**
     * 修改用户的密码
     * 
     * @param u
     *            用户
     * @param passwd
     *            密码
     */
    void setPassword(WnUsr u, String passwd);

    void set(WnUsr u, String key, Object val);

    void set(WnUsr u, NutMap meta);

    /**
     * 删除一个用户
     * 
     * @param u
     *            用户
     */
    void delete(WnUsr u);

    WnUsr fetch(String str);

    WnUsr check(String str);

    WnUsr fetchBy(WnUsrInfo info);

    WnUsr checkBy(WnUsrInfo info);

    int getRoleInGroup(WnUsr u, String grp);

    boolean isMemberOfGroup(WnUsr u, String... grps);

    boolean isAdminOfGroup(WnUsr u, String... grps);

    void setRoleInGroup(WnUsr u, String grp, int role);

    int removeRoleFromGroup(WnUsr u, String grp);

    List<String> findMyGroups(WnUsr u);

    boolean isInGroup(WnUsr u, String grp);

    void eachInGroup(String grp, WnQuery q, Each<WnRole> callback);

    List<WnUsr> queryInGroup(String grp, WnQuery q);

    void each(WnQuery q, Each<WnUsr> callback);

    List<WnUsr> query(WnQuery q);

}