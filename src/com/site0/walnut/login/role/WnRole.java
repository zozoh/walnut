package com.site0.walnut.login.role;

import java.util.Map;

import org.nutz.lang.util.NutBean;

/**
 * 用户权限，相当于一个用户对于某一事物的权限声明
 * <p>
 * 所谓事物，我们通常用一个抽象的组名来代替，譬如 WnObj 的 grp 就表示自己的抽象组名 <br>
 * 一个用户(uid)对于一个事物(grp)到底扮演了什么角色呢？我们认为有下列可能
 * <ul>
 * <li><code>ADMIN</code> 【所有者】或者说是【管理员】，通常它具备无上权限
 * <li><code>MEMEBER</code> 【协作者】或者说是【成员】
 * <li><code>GUEST</code> 【关注者】或者说是【访客】
 * <li><code>BLOCK</code> 【破坏者】或者说是【黑名单成员】
 * </ul>
 * 这样任何事物都可以针对这四种角色指明其真正能做的事情。<Br>
 * 这与 Linux/Unix 的文件权限码 <code>755 rwxrwxrwx</code> 可以很好的对应。
 * 
 * <p>
 * <blockquote> 对于 BLOCK 的用户，我们自然不需要权限码，它要做什么都完全禁止就好 </blockquote>
 * <p>
 * 
 * <h2>用户特权</h2>
 * <p>
 * 我们这个权限设定，都是【用户】对于【事物】是什么【身份/角色：ADMIN|MEMEMBER...】<br>
 * 通常，它会工作的很好，但是这也会带来一定的局限性:
 * <p>
 * 譬如某个系统，角色是按照部门划分: IT,RD,OP,FA ... <br>
 * 我们可以指定某用户为 IT+ADMIN 表示它是 IT 部主管，这非常符合直觉。<br>
 * 但是，如果我想让 IT+ADMIN 与 RD+ADMIN|MEMBER 都可以访问系统一个特殊的的接口，<br>
 * 我不得不在入口处埋点，指明只有 IT+ADMIN 与 RD+ADMIN|MEMBER 才能访问。<br>
 * 同时更糟糕的是，如果有一天我们需要调整这个设置，所有埋点处都需要修改，这就是潜藏的 Bug 温床。
 * <p>
 * 为此聪明的你，肯定会想到一条妙计：建立一个单独的角色组【special】任何加入这个组的用户，
 * 都可以访问这个特殊接口，调整的时候，仅需要调整这个组的成员，埋点处无需任何调整。
 * <p>
 * 对于大多数用户来说，这当然是一个可以接受的方案，但是如果我们做的更优雅一些，<br>
 * 我们应该让权限的设置更加符合用户的心智模型，在用户心中一定是这样想的：<br>
 * <code>我想让IT主管还有开发那帮人可以访问这个特殊接口</code> 因此，如果我们为每个用户角色添加一个特殊字段:
 * <code>privileges</code>， 一个用半角逗号分隔的抽线权限名称，就能完美的解决这个问题了。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnRole {

    String getId();

    String getGroup();

    WnRoleType getType();

    boolean isMember();

    boolean isAdmin();

    boolean isMatchName(String name);

    NutBean toBean();

    void setUserId(String userId);

    String getUserId();

    void setUserName(String userName);

    String getUserName();

    boolean hasUserName();

    boolean hasUserId();

    void fromBean(NutBean oRole);

    Map<String, Boolean> getPrivileges();

    boolean hasPrivileges();

    void mergePrivilegesTo(Map<String, Boolean> map);

}
