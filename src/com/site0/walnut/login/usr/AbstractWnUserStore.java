package com.site0.walnut.login.usr;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.login.WnUserStore;
import com.site0.walnut.util.Ws;

public abstract class AbstractWnUserStore implements WnUserStore {

    protected UserRace userRace;

    protected NutBean defaultMeta;

    @Override
    public UserRace getUserRace() {
        return userRace;
    }

    protected void join_user_meta_to_bean(WnUser u, NutMap bean) {
        if (null != u.getMeta()) {
            for (Map.Entry<String, Object> en : u.getMeta().entrySet()) {
                String key = Ws.snakeCase(en.getKey());
                Object val = en.getValue();
                bean.put(key, val);
            }
        }
    }

    protected WnSimpleUser toWnUser(NutBean bean) {
        WnSimpleUser u = new WnSimpleUser(bean);
        u.setUserRace(this.userRace);
        u.putMetas(this.defaultMeta);
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
        WnUser info = new WnSimpleUser(nameOrPhoneOrEmail);
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
        NutMap qmap = info.toBean();
        WnQuery q = new WnQuery();
        q.setAll(qmap);
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
