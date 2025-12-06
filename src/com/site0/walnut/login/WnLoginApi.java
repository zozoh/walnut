package com.site0.walnut.login;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.role.WnRole;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.role.WnRoleStore;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSessionStore;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnUserStore;

public interface WnLoginApi {

    // WnRoleList getRoles(WnUser u);
    //
    // WnRoleList queryRolesOf(String name);
    //
    // WnRoleType getRoleTypeOfGroup(WnUser u, String group);
    WnRoleLoader roleLoader(WnSession se);

    WnRole addRole(WnUser u, String grp, WnRoleType type);

    int getSessionDuration();

    int getSessionDuration(boolean longSession);

    String getWechatGhOpenIdKey();

    String getWechatMpOpenIdKey();

    WnXApi getXapi();

    WnRoleStore getRoleStore();

    WnSessionStore getSessionStore();

    WnUserStore getUserStore();

    void changePassword(WnUser u, String rawPassword);

    void removeRole(WnRole role);

    void removeRole(String uid, String name);

    UserRace getUserRace();

    WnUser addRootUserIfNoExists(String dftPassword);

    WnUser addGuestUserIfNoExists();

    WnUser addUser(WnUser u);

    List<WnUser> queryUser(WnQuery q);

    WnUser getUser(String nameOrPhoneOrEmail);

    WnUser checkUser(String nameOrPhoneOrEmail);

    WnUser getUser(WnUser info);

    WnUser checkUser(WnUser info);

    WnUser getUserById(String uid);

    WnUser checkUserById(String uid);

    void saveUserMeta(WnUser u);

    void updateUserName(WnUser u);

    void updateUserPhone(WnUser u);

    void updateUserEmail(WnUser u);

    void updateUserLastLoginAt(WnUser u);

    void updateUserPassword(WnUser u, String rawPassword);

    WnObj updateUserHomeName(WnUser u, String newName);

    WnSession createSession(WnUser u, String type);

    WnSession createSession(WnUser u, String type, int duInSec);

    WnSession createSession(WnSession parentSe, WnUser u, String type, int duInSec);

    WnSession removeSession(WnSession se);

    WnSession loginByPassword(String nameOrPhoneOrEmail, String rawPassword);

    WnSession loginByWechatMPCode(String code, boolean autoCreateUser);

    WnSession logout(String ticket);

    WnSession getSession(String ticket);

    WnSession getSessionByUserNameAndType(String unm, String type);

    WnSession getSessionByUserIdAndType(String uid, String type);

    List<WnSession> querySession(WnQuery q);

    WnSession checkSession(String ticket);

    void saveSessionEnv(WnSession se);

    void touchSession(WnSession se);

}