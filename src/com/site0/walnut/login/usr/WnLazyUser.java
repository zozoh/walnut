package com.site0.walnut.login.usr;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.role.WnRoleRank;
import com.site0.walnut.login.role.WnRoleList;

public class WnLazyUser implements WnUser {

    private WnUserStore loader;

    private WnUser _user;

    private boolean fullLoaded;

    public WnLazyUser(WnUserStore userLoader) {
        this.loader = userLoader;
        this.fullLoaded = false;
    }

    @Override
    public WnRoleRank getRank(WnRoleList roles) {
        if (null == _user) {
            throw Er.create("e.usr.lazy.WithoutInnerUserWhenGetRank");
        }
        return _user.getRank(roles);
    }

    @Override
    public boolean isSame(WnUser u) {
        if (null == u) {
            return false;
        }
        return _user.isSame(u);
    }

    @Override
    public boolean isSameId(String userId) {
        return _user.isSameId(userId);
    }

    @Override
    public boolean isSameName(String userName) {
        return _user.isSameName(userName);
    }

    @Override
    public boolean isSysUser() {
        return UserRace.SYS == this.getUserRace();
    }

    @Override
    public boolean isDomainUser() {
        return UserRace.DOMAIN == this.getUserRace();
    }

    @Override
    public WnLazyUser clone() {
        WnLazyUser re = new WnLazyUser(loader);
        re.fullLoaded = this.fullLoaded;
        re._user = this._user.clone();
        return re;
    }

    public void setInnerUser(WnUser u) {
        this.fullLoaded = false;
        this._user = u;

    }

    public void setInnerUser(UserRace race, String uid, String name, String email, String phone) {
        this.fullLoaded = false;
        WnUser u = new WnSimpleUser();
        u.setUserRace(race);
        u.setId(uid);
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        this._user = u;

    }

    synchronized private void reloadInnerUser() {
        if (!this.fullLoaded) {
            this._user = loader.checkUserById(_user.getId());
            this.fullLoaded = true;
        }
    }

    public void updateBy(NutBean bean) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.updateBy(bean);
    }

    public void setLoginStr(String str, boolean autoSetName) {
        _user.setLoginStr(str, autoSetName);
    }

    public String toString() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.toString();
    }

    public NutMap toBean() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.toBean();
    }

    public void mergeToBean(NutBean bean) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.mergeToBean(bean);
    }

    public UserRace getUserRace() {
        return _user.getUserRace();
    }

    public void setUserRace(UserRace userRace) {
        _user.setUserRace(userRace);
    }

    public boolean hasId() {
        return _user.hasId();
    }

    public String getId() {
        return _user.getId();
    }

    public void setId(String id) {
        _user.setId(id);
    }

    public String getName() {
        return _user.getName();
    }

    public void setName(String name) {
        _user.setName(name);
    }

    public String getPhone() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getPhone();
    }

    public void setPhone(String phone) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setPhone(phone);
    }

    public String getEmail() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getEmail();
    }

    public void setEmail(String email) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setEmail(email);
    }

    public long getLastLoginAt() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getLastLoginAt();
    }

    @Override
    public String getLastLoginAtInUTC() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getLastLoginAtInUTC();
    }

    public void setLastLoginAt(long lastLoginAt) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setLastLoginAt(lastLoginAt);
    }

    public String getMainGroup() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getMainGroup();
    }

    public void setMainGroup(String mainGroup) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setMainGroup(mainGroup);
    }

    public String[] getRoles() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getRoles();
    }

    public void setRoles(String[] roleNames) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setRoles(roleNames);
    }

    public boolean hasMeta() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.hasMeta();
    }

    public NutBean getMeta() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getMeta();
    }

    public void setMeta(NutBean meta) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setMeta(meta);
    }

    public void putMetas(NutBean meta) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.putMetas(meta);
    }

    public String getPasswd() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getPasswd();
    }

    public void setPasswd(String passwd) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setPasswd(passwd);
    }

    public String getSalt() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getSalt();
    }

    public void setSalt(String salt) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setSalt(salt);
    }

    @Override
    public void genSaltAndRawPasswd(String rawPasswd) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.genSaltAndRawPasswd(rawPasswd);
    }

    @Override
    public boolean hasSaltedPasswd() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.hasSaltedPasswd();
    }

    @Override
    public void setRawPasswd(String passwd) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setRawPasswd(passwd);
    }

    public boolean isMatchedRawPasswd(String passwd) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.isMatchedRawPasswd(passwd);
    }

    @Override
    public String getMetaString(String key, String dft) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getMetaString(key, dft);
    }

    @Override
    public String getMetaString(String key) {
        return this.getMetaString(key, null);
    }

    @Override
    public String getHomePath() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getHomePath();
    }

    @Override
    public void setHomePath(String path) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setHomePath(path);
    }

    @Override
    public boolean isSameMainGroup(String mainGroup) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.isSameMainGroup(mainGroup);
    }

    @Override
    public void setMeta(String key, Object val) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.setMeta(key, val);

    }

    @Override
    public void removeMeta(String... keys) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.removeMeta(keys);

    }

}
