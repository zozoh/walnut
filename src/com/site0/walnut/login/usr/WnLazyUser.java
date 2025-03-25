package com.site0.walnut.login.usr;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.login.WnUserStore;

public class WnLazyUser implements WnUser {

    private WnUserStore loader;

    private WnUser _user;

    private boolean fullLoaded;

    public WnLazyUser(WnUserStore userLoader) {
        this.loader = userLoader;
        this.fullLoaded = false;
    }

    public void setInnerUser(WnUser u) {
        this.fullLoaded = false;
        this._user = u;

    }

    public void setInnerUser(UserRace race, String uid, String name, String email, String phone) {
        this.fullLoaded = false;
        WnSimpleUser u = new WnSimpleUser();
        u.setUserRace(race);
        u.setId(uid);
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        this._user = u;

    }

    private void reloadInnerUser() {
        this._user = loader.checkUserById(_user.getId());
        this.fullLoaded = true;
    }

    public void updateBy(NutBean bean) {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        _user.updateBy(bean);
    }

    public void setLoginStr(String str) {
        _user.setLoginStr(str);
    }

    public String toString() {
        return _user.toString();
    }

    public NutMap toBean() {
        return _user.toBean();
    }

    public void mergeToBean(NutBean bean) {
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
        return _user.getPhone();
    }

    public void setPhone(String phone) {
        _user.setPhone(phone);
    }

    public String getEmail() {
        return _user.getEmail();
    }

    public void setEmail(String email) {
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
        _user.setLastLoginAt(lastLoginAt);
    }

    public String getMainGroup() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getMainGroup();
    }

    public void setMainGroup(String mainGroup) {
        _user.setMainGroup(mainGroup);
    }

    public String[] getRoles() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getRoles();
    }

    public void setRoles(String[] roleNames) {
        _user.setRoles(roleNames);
    }

    public boolean hasMeta() {
        return _user.hasMeta();
    }

    public NutBean getMeta() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getMeta();
    }

    public void setMeta(NutBean meta) {
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
        _user.setPasswd(passwd);
    }

    public String getSalt() {
        if (!this.fullLoaded) {
            this.reloadInnerUser();
        }
        return _user.getSalt();
    }

    public void setSalt(String salt) {
        _user.setSalt(salt);
    }

}
