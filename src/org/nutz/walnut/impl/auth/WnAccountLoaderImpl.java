package org.nutz.walnut.impl.auth;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAccountLoader;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;

public class WnAccountLoaderImpl implements WnAccountLoader {

    private WnIo io;

    private WnObj oAccountDir;

    public WnAccountLoaderImpl(WnIo io, WnObj oAccountDir) {
        this.io = io;
        this.oAccountDir = oAccountDir;
    }

    @Override
    public List<WnAccount> queryAccount(WnQuery q) {
        // 默认
        if (null == q) {
            q = Wn.Q.pid(oAccountDir);
        }
        // 强制设置 pid
        else {
            q.setv("pid", oAccountDir.id());
        }
        if (!q.hasLimit()) {
            q.limit(100);
        }

        List<WnObj> objs = io.query(q);
        List<WnAccount> list = new ArrayList<>(objs.size());
        for (WnObj obj : objs) {
            list.add(new WnAccount(obj));
        }

        return list;

    }

    @Override
    public WnAccount getAccount(String nameOrIdOrPhoneOrEmail) {
        WnAccount info = new WnAccount(nameOrIdOrPhoneOrEmail);
        return this.getAccount(info);
    }

    @Override
    public WnAccount checkAccount(String nameOrIdOrPhoneOrEmail) {
        WnAccount u = this.getAccount(nameOrIdOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", nameOrIdOrPhoneOrEmail);
        }
        return u;
    }

    @Override
    public WnAccount getAccount(WnAccount info) {
        // 用 ID 获取
        if (info.hasId()) {
            String uid = info.getId();
            return this.checkAccountById(uid);
        }
        // 将信息转换为查询条件
        // 通常这个信息是手机号/邮箱/登录名等
        NutMap qmap = info.toBean(WnAuths.ABMM.LOGIN);
        WnQuery q = Wn.Q.pid(oAccountDir);
        q.setAll(qmap);
        q.limit(10);

        // 获取账户信息的备选列表
        List<WnObj> list = io.query(q);

        for (WnObj oU : list) {
            // 无视过期的对象
            if (oU.isExpired()) {
                continue;
            }
            // 忽略已经被删除的 Thing
            if (oU.getInt("th_live", 1) == -1) {
                continue;
            }
            // 嗯就是要找的
            return new WnAccount(oU);
        }

        // 神马都木有查到 ...
        return null;
    }

    @Override
    public WnAccount checkAccount(WnAccount info) {
        WnAccount u = this.getAccount(info);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", info);
        }
        return u;
    }

    @Override
    public WnAccount getAccountById(String uid) {
        WnObj oU = io.get(uid);
        if (null != oU && oU.getInt("th_live", 1) != -1) {
            if (!oAccountDir.isSameId(oU.parentId())) {
                throw Er.create("e.auth.acc_outof_home", uid);
            }
            return new WnAccount(oU);
        }
        return null;
    }

    @Override
    public WnAccount checkAccountById(String uid) {
        WnAccount u = this.getAccountById(uid);
        if (null == u) {
            throw Er.create("e.auth.noexists", uid);
        }
        return u;
    }

}
