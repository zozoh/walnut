package org.nutz.walnut.impl.io;

import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnGroupRole;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.AbstractWnSecurity;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnSecurityImpl extends AbstractWnSecurity {

    private WnAuthService auth;

    private WnEvalLink _eval_link;

    public WnSecurityImpl(WnIo io, WnAuthService auth) {
        super(io);
        this.auth = auth;
        this._eval_link = new WnEvalLink(io);
    }

    @Override
    public WnObj enter(WnObj nd, boolean asNull) {
        return Wn.WC().security(_eval_link, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = __eval_obj(nd);
                return __do_check(o, Wn.Io.RX, asNull);
            }
        });
    }

    @Override
    public WnObj access(WnObj nd, boolean asNull) {
        return Wn.WC().security(_eval_link, new Proton<WnObj>() {
            protected WnObj exec() {
                return __do_check(nd, Wn.Io.R, asNull);
            }
        });
    }

    @Override
    public WnObj read(WnObj nd, boolean asNull) {
        return Wn.WC().security(_eval_link, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = __eval_obj(nd);
                return __do_check(o, Wn.Io.R, asNull);
            }
        });
    }

    @Override
    public WnObj write(WnObj nd, boolean asNull) {
        return Wn.WC().security(_eval_link, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = __eval_obj(nd);
                return __do_check(o, Wn.Io.W, asNull);
            }
        });
    }

    @Override
    public WnObj meta(WnObj nd, boolean asNull) {
        return Wn.WC().security(_eval_link, new Proton<WnObj>() {
            protected WnObj exec() {
                return __do_check(nd, Wn.Io.W, false);
            }
        });
    }

    @Override
    public WnObj remove(WnObj nd, boolean asNull) {
        return Wn.WC().security(_eval_link, new Proton<WnObj>() {
            protected WnObj exec() {
                // 父目录可写可访问
                if (nd.hasParent())
                    __do_check(nd.parent(), Wn.Io.WX, asNull);

                // 自己可写
                return __do_check(nd, Wn.Io.W, asNull);
            }
        });
    }

    protected WnObj __do_check(WnObj o, int mask, boolean asNull) {
        // 防止空指针
        if (null == o)
            return null;

        // 如果对象过期了，抛错
        if (o.isExpired()) {
            if (asNull)
                return null;
            throw Er.create("e.io.obj.expired", o);
        }

        // 当前的线程上下文
        WnContext wc = Wn.WC();

        // 我是谁？
        WnAccount u = wc.getMe();

        // // 对于 root 用户，啥都不检查
        // if ("root".equals(u.name()))
        // return o;
        //

        // 对于 root 组成员，啥都不检查
        if (auth.isMemberOfGroup(u, "root"))
            return o;

        // 自定义权限优先
        int md = o.getCustomizedPrivilege(u);

        // 采用默认的权限码
        if (md == Wn.Io.NO_PVG) {
            md = o.mode();
        }

        // 本身就是创建者，那么看看 u 部分的权限
        if (o.creator().equals(u.getName())) {
            if (((md >> 6) & mask) == mask)
                return o;
        }

        // 对象组给我啥权限
        WnGroupRole role = auth.getGroupRole(u, o.group());

        // 黑名单的话，禁止
        if (WnGroupRole.BLOCK == role) {
            if (asNull)
                return null;
            throw Er.create("e.io.forbidden");
        }

        // o 允许进入
        if ((md & mask) == mask)
            return o;

        // g 允许进入
        if (WnGroupRole.MEMBER == role && ((md >> 3) & mask) == mask)
            return o;

        // u 允许进入
        if (WnGroupRole.ADMIN == role && ((md >> 6) & mask) == mask)
            return o;

        // 看看是否允许为空
        if (asNull)
            return null;

        // 抛错，没有权限
        throw Er.create("e.io.forbidden", o.path());
    }

    @Override
    public boolean test(WnObj nd, int mode) {
        WnContext wc = Wn.WC();
        wc.setSecurity(null);
        try {
            WnObj o = __eval_obj(nd);
            return null != __do_check(o, mode, true);
        }
        finally {
            wc.setSecurity(this);
        }
    }

}
