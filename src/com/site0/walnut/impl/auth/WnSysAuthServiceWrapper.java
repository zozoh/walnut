package com.site0.walnut.impl.auth;

import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.auth.WnAuthSetup;
import com.site0.walnut.api.auth.WnGroupAccount;
import com.site0.walnut.api.auth.WnGroupRole;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.util.Wn;

public class WnSysAuthServiceWrapper implements WnAuthService {

    // It will be injected by Ioc
    private WnIo io;
    private NutMap initEnvs;
    private String rootDefaultPasswd;
    private int seDftDu;
    private int seTmpDu;
    private String defaultQuitUrl;

    // 下面这俩是 on_create 创建的
    private WnAuthServiceImpl impl;
    private WnAccount root;

    public void on_create() {
        WnAuthSetup setup = new WnSysAuthSetup(io, seDftDu, seTmpDu);
        impl = new WnAuthServiceImpl(io, setup);
        impl.setDefaultQuitUrl(defaultQuitUrl);
        // 无论如何，确保有根用户
        root = impl.getAccount("root");
        if (null == root) {
            WnAccount u = new WnAccount();
            u.setName("root");
            u.setRawPasswd(rootDefaultPasswd);
            if (null != initEnvs) {
                u.putAllMeta(initEnvs);
            }
            root = impl.createAccount(u);
        }
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setInitEnvs(NutMap initEnvs) {
        this.initEnvs = initEnvs;
    }

    public void setRootDefaultPasswd(String rootDefaultPasswd) {
        this.rootDefaultPasswd = rootDefaultPasswd;
    }

    public void setSeDftDu(int seDftDu) {
        this.seDftDu = seDftDu;
    }

    public void setSeTmpDu(int seTmpDu) {
        this.seTmpDu = seTmpDu;
    }

    public void setDefaultQuitUrl(String defaultQuitUrl) {
        this.defaultQuitUrl = defaultQuitUrl;
    }

    public WnAccount createAccount(WnAccount user) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                // 加入默认环境变量
                if (null != initEnvs) {
                    user.putAllMeta(initEnvs);
                }
                // 创建吧
                return impl.createAccount(user);
            }
        });
    }

    public List<WnAccount> queryAccount(WnQuery q) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<List<WnAccount>>() {
            protected List<WnAccount> exec() {
                return impl.queryAccount(q);
            }
        });
    }

    public WnAccount getAccount(String nameOrPhoneOrEmail) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.getAccount(nameOrPhoneOrEmail);
            }
        });
    }

    public void saveAccount(WnAccount user) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.saveAccount(user);
        });
    }

    public WnObj getAvatarObj(WnAccount user, boolean autoCreate) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnObj>() {
            protected WnObj exec() {
                return impl.getAvatarObj(user, autoCreate);
            }
        });
    }

    public WnAccount checkAccount(String nameOrPhoneOrEmail) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.checkAccount(nameOrPhoneOrEmail);
            }
        });
    }

    public void saveAccount(WnAccount user, int mode) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.saveAccount(user, mode);
        });
    }

    public WnAccount saveAccount(WnAccount user, NutMap meta) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.saveAccount(user, meta);
            }
        });
    }

    public WnAccount getAccount(WnAccount info) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.getAccount(info);
            }
        });
    }

    public WnAccount checkAccount(WnAccount info) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.checkAccount(info);
            }
        });
    }

    public WnAccount checkAccountById(String uid) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.checkAccountById(uid);
            }
        });
    }

    public WnAccount getAccountById(String uid) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAccount>() {
            protected WnAccount exec() {
                return impl.getAccountById(uid);
            }
        });
    }

    public void renameAccount(WnAccount user, String newName) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.renameAccount(user, newName);
        });
    }

    public void deleteAccount(WnAccount user) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.deleteAccount(user);
        });
    }

    public WnObj getSysRoleDir() {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnObj>() {
            protected WnObj exec() {
                return impl.getSysRoleDir();
            }
        });
    }

    public WnGroupRole getGroupRole(WnAccount user, String groupName) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnGroupRole>() {
            protected WnGroupRole exec() {
                return impl.getGroupRole(user, groupName);
            }
        });
    }

    public void setGroupRole(WnAccount user, String groupName, WnGroupRole role) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.setGroupRole(user, groupName, role);
        });
    }

    public WnGroupRole removeGroupRole(WnAccount user, String groupName) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnGroupRole>() {
            protected WnGroupRole exec() {
                return impl.removeGroupRole(user, groupName);
            }
        });
    }

    public List<WnGroupAccount> getAccounts(String groupName) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<List<WnGroupAccount>>() {
            protected List<WnGroupAccount> exec() {
                return impl.getAccounts(groupName);
            }
        });
    }

    public WnAuthSession getSession(String ticket) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.getSession(ticket);
            }
        });
    }

    public WnAuthSession getSession(String byType, String byValue) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.getSession(byType, byValue);
            }
        });
    }

    public List<WnGroupAccount> getGroups(WnAccount user) {
        return Wn.WC().nosecurity(impl.io, new Proton<List<WnGroupAccount>>() {
            protected List<WnGroupAccount> exec() {
                return impl.getGroups(user);
            }
        });
    }

    public boolean isRoleOfGroup(WnGroupRole role, WnAccount user, String... groupNames) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<Boolean>() {
            protected Boolean exec() {
                return impl.isRoleOfGroup(role, user, groupNames);
            }
        });
    }

    public boolean isAdminOfGroup(WnAccount user, String... groupNames) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<Boolean>() {
            protected Boolean exec() {
                return impl.isAdminOfGroup(user, groupNames);
            }
        });
    }

    public boolean isMemberOfGroup(WnAccount user, String... groupNames) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<Boolean>() {
            protected Boolean exec() {
                return impl.isMemberOfGroup(user, groupNames);
            }
        });
    }

    public WnAuthSession checkSession(String ticket) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.checkSession(ticket);
            }
        });
    }

    public WnAuthSession checkSession(String byType, String byValue) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.checkSession(byType, byValue);
            }
        });
    }

    public WnAuthSession touchSession(WnAuthSession se) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.touchSession(se);
            }
        });
    }

    public WnAuthSession createSession(WnAccount user, boolean longSession) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.createSession(user, longSession);
            }
        });
    }

    public WnAuthSession createSession(WnAccount user, int se_du) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.createSession(user, se_du);
            }
        });
    }

    public WnAuthSession createSession(WnAuthSession pse, WnAccount user, int se_du) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.createSession(pse, user, se_du);
            }
        });
    }

    public void saveSession(WnAuthSession se) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.saveSession(se);
        });
    }

    public void saveSessionInfo(WnAuthSession se) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.saveSessionInfo(se);
        });
    }

    public void saveSessionVars(WnAuthSession se) {
        Wn.WC().suCoreNoSecurity(impl.io, root, () -> {
            impl.saveSessionVars(se);
        });
    }

    public WnAuthSession removeSession(WnAuthSession se, long delay) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.removeSession(se, delay);
            }
        });
    }

    public WnAuthSession loginByWxCode(String code, String wxCodeType, boolean forbidUnsubscribe) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.loginByWxCode(code, wxCodeType, forbidUnsubscribe);
            }
        });
    }

    public WnAuthSession bindAccount(String nameOrIdOrPhoneOrEmail,
                                     String scene,
                                     String vcode,
                                     String ticket) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.bindAccount(nameOrIdOrPhoneOrEmail, scene, vcode, ticket);
            }
        });
    }

    public WnAuthSession loginByVcode(String phoneOrEmail, String scene, String vcode) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.loginByVcode(phoneOrEmail, scene, vcode);
            }
        });
    }

    public WnAuthSession loginByPasswd(String nameOrIdOrPhoneOrEmail, String passwd) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.loginByPasswd(nameOrIdOrPhoneOrEmail, passwd);
            }
        });
    }

    public WnAuthSession logout(String ticket, long delay) {
        return Wn.WC().suCoreNoSecurity(impl.io, root, new Proton<WnAuthSession>() {
            protected WnAuthSession exec() {
                return impl.logout(ticket, delay);
            }
        });
    }

}
