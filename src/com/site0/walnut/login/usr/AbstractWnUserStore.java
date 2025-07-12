package com.site0.walnut.login.usr;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.util.Ws;

public abstract class AbstractWnUserStore implements WnUserStore {

    protected UserRace userRace;

    protected NutMap defaultMeta;

    protected AbstractWnUserStore(UserRace userRace, NutMap defaultMeta) {
        this.defaultMeta = defaultMeta;
        this.userRace = userRace;
    }

    @Override
    public UserRace getUserRace() {
        return userRace;
    }

    public NutMap getDefaultMeta() {
        return defaultMeta;
    }

    public void setDefaultMeta(NutMap defaultMeta) {
        this.defaultMeta = defaultMeta;
    }

    public void patchDefaultEnv(WnUser u) {
        NutBean meta = null == this.defaultMeta ? new NutMap() : this.defaultMeta.duplicate();
        if (null == u.getMeta()) {
            u.setMeta(meta);
        }
        // 融合
        else {
            u.getMeta().putAll(meta);
        }
    }

    protected WnUser toWnUser(NutBean bean) {
        WnUser u = new WnSimpleUser(bean);
        if (null != this.defaultMeta) {
            u.putMetas(this.defaultMeta);
        }
        u.setUserRace(this.userRace);
        return u;
    }

    @Override
    public WnUser getUser(WnQuery q) {
        q.limit(2);
        List<WnUser> ulist = this.queryUser(q);
        return __get_one_account_from_list(ulist, q.toString());
    }

    @Override
    public WnUser checkUser(WnQuery q) {
        WnUser u = this.getUser(q);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", q.toString());
        }
        return u;
    }

    @Override
    public WnUser getUser(String nameOrPhoneOrEmail) {
        WnUser info = new WnSimpleUser();
        info.setLoginStr(nameOrPhoneOrEmail, false);
        return this.getUser(info);
    }

    @Override
    public WnUser checkUser(String nameOrPhoneOrEmail) {
        WnUser u = this.getUser(nameOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", nameOrPhoneOrEmail);
        }
        return u;
    }

    @Override
    public WnUser getUser(WnUser info) {
        // 用 ID 获取
        if (info.hasId()) {
            String uid = info.getId();
            return this.getUserById(uid);
        }
        // 将信息转换为查询条件
        // 通常这个信息是手机号/邮箱/登录名等
        WnQuery q = new WnQuery();

        // Name
        String name = info.getName();
        if (!Ws.isBlank(name))
            q.setv("nm", name);

        // 电话
        String phone = info.getPhone();
        if (!Ws.isBlank(phone))
            q.setv("phone", phone);

        // 邮箱
        String email = info.getEmail();
        if (!Ws.isBlank(email))
            q.setv("email", email);

        // 其他元数据
        if (info.hasMeta()) {
            q.setAll(info.getMeta());
        }

        // 查询超过2个就没意义了
        q.limit(2);

        List<WnUser> list = queryUser(q);
        return __get_one_account_from_list(list, "getAccount:" + info.toString());
    }

    protected WnUser __get_one_account_from_list(List<WnUser> list, String hint) {
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            throw Er.create("e.auth.account.multiExists", hint);
        }
        return list.get(0);
    }

    @Override
    public WnUser checkUser(WnUser info) {
        WnUser u = this.getUser(info);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", info.toBean());
        }
        return u;
    }

    @Override
    public WnUser checkUserById(String uid) {
        WnUser u = this.getUserById(uid);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", uid);
        }
        return u;
    }
}
