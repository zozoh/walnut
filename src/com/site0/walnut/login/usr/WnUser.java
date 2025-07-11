package com.site0.walnut.login.usr;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.role.WnRoleRank;
import com.site0.walnut.login.role.WnRoleList;

/**
 * 对于 id,phone,name,email 登录和容易理解，对于微信等第三方登录这里需要说明一下。
 * 
 * 考虑到一个账号可能要支持多种第三方登录，我们支持的策略就是讲第三方的id （譬如 wx openID） 存储在账号的数据表里。在本对象上，全都收缩进
 * meta 字段。
 * 
 * 这样，数据表里的字段与 meta 的字段关系，是各个的 Loader 来负责的。
 * 
 * 对于系统其他核心逻辑部分，也并不关心
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnUser {

    WnRoleRank getRank(WnRoleList roles);

    boolean isSame(WnUser u);

    boolean isSameId(String userId);

    boolean isSameName(String userName);

    boolean isSameMainGroup(String mainGroup);

    void updateBy(NutBean bean);

    void setLoginStr(String str);

    String toString();

    NutMap toBean();

    void mergeToBean(NutBean bean);

    boolean isSysUser();

    boolean isDomainUser();

    UserRace getUserRace();

    void setUserRace(UserRace userRace);

    boolean hasId();

    String getId();

    void setId(String id);

    String getName();

    void setName(String name);

    String getPhone();

    void setPhone(String phone);

    String getEmail();

    void setEmail(String email);

    long getLastLoginAt();

    String getLastLoginAtInUTC();

    void setLastLoginAt(long lastLoginAt);

    String getMainGroup();

    void setMainGroup(String mainGroupName);

    String[] getRoles();

    void setRoles(String[] roleNames);

    boolean hasMeta();

    NutBean getMeta();

    void setMeta(NutBean meta);

    void putMetas(NutBean meta);

    String getPasswd();

    void setPasswd(String passwd);

    String getSalt();

    void setSalt(String salt);

    WnUser clone();

    String getMetaString(String key, String dft);

    String getMetaString(String key);

    String getHomePath();

    void setHomePath(String path);

    void genSaltAndRawPasswd(String rawPasswd);

    void setMeta(String key, Object val);

    void removeMeta(String... keys);

    boolean hasSaltedPasswd();

    boolean isMatchedRawPasswd(String passwd);

    void setRawPasswd(String passwd);

}