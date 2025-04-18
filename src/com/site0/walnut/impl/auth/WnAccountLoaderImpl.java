package com.site0.walnut.impl.auth;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAccountLoader;
import com.site0.walnut.api.auth.WnAuths;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.util.Wn;

public class WnAccountLoaderImpl implements WnAccountLoader {

    private WnIo io;

    private WnObj oAccountDir;

    /**
     * 确保账户对象在自己的 oAccountDir 里
     */
    private boolean forceInHome;

    public WnAccountLoaderImpl(WnIo io, WnObj oAccountDir, boolean forceInHome) {
        this.io = io;
        this.oAccountDir = oAccountDir;
        this.forceInHome = forceInHome;
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
    public WnAccount getAccount(String nameOrPhoneOrEmail) {
        WnAccount info = new WnAccount(nameOrPhoneOrEmail);
        return this.getAccount(info);
    }

    @Override
    public WnAccount checkAccount(String nameOrPhoneOrEmail) {
        WnAccount u = this.getAccount(nameOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", nameOrPhoneOrEmail);
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
        NutMap qmap = info.toBean(WnAuths.ABMM.QUERY_INFO);
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
        // 考虑两段式 ID，确保起始段一定为 oAccountDir
        WnObjId oid = new WnObjId(uid);

        // 读取用户
        WnObj oU = io.getIn(oAccountDir, oid.getMyId());
        if (null != oU && oU.getInt("th_live", 1) != -1) {
            if (this.forceInHome && !oAccountDir.isSameId(oU.parentId())) {
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
