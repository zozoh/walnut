package org.nutz.walnut.impl.io;

import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnSecurityImpl extends WnEvalLink {

    private WnUsrService usrs;

    public WnSecurityImpl(WnIo io, WnUsrService usrs) {
        super(io);
        this.usrs = usrs;
    }

    @Override
    public WnObj enter(WnObj nd) {
        return Wn.WC().security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = __eval_obj(nd);
                return __do_check(o, Wn.Io.RX, false);
            }
        });
    }

    @Override
    public WnObj access(WnObj nd) {
        return Wn.WC().security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                return __do_check(nd, Wn.Io.R, true);
            }
        });
    }

    @Override
    public WnObj remove(WnObj nd) {
        return Wn.WC().security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                // 父目录可写可访问
                if (nd.hasParent())
                    __do_check(nd.parent(), Wn.Io.WX, false);

                // 自己可写
                return __do_check(nd, Wn.Io.W, false);
            }
        });
    }

    // @Override
    // public WnObj view(WnObj nd) {
    // WnContext wc = Wn.WC();
    // wc.setSecurity(null);
    // try {
    // WnObj o = __eval_obj(nd, false);
    // return __do_check(o, Wn.Io.R, true);
    // }
    // finally {
    // wc.setSecurity(this);
    // }
    // }

    @Override
    public WnObj read(WnObj nd) {
        return Wn.WC().security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = __eval_obj(nd);
                return __do_check(o, Wn.Io.R, false);
            }
        });
    }

    @Override
    public WnObj write(WnObj nd) {
        return Wn.WC().security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = __eval_obj(nd);
                return __do_check(o, Wn.Io.W, false);
            }
        });
    }

    @Override
    public WnObj meta(WnObj nd) {
        return Wn.WC().security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                return __do_check(nd, Wn.Io.W, false);
            }
        });
    }

    private WnObj __do_check(WnObj o, int mask, boolean asNull) {

        // 防止空指针
        if (null == o)
            return null;

        // 我是谁？
        String me = Wn.WC().checkMe();
        WnUsr u = usrs.check(me);

        // 对于 root 用户，啥都不检查
        if ("root".equals(u.name()))
            return o;

        // 如果对象过期了，抛错
        if (o.isExpired()) {
            if (asNull)
                return null;
            throw Er.create("e.io.obj.expired", o);
        }

        // 对象组给我啥权限
        int role = usrs.getRoleInGroup(u, o.group());

        // 黑名单的话，禁止
        if (Wn.ROLE.BLOCK == role) {
            if (asNull)
                return null;
            throw Er.create("e.io.forbidden");
        }

        // 对象的权限设定
        // TODO zozoh: 这里考虑一下 /grp/$grp/pvg 下的权限设定
        // 或许给 WnUsrService 加个函数，带点缓存就是了 ...
        int md = o.mode();

        // o 允许进入
        if ((md & mask) == mask)
            return o;

        // g 允许进入
        if (Wn.ROLE.MEMBER == role && ((md >> 3) & mask) == mask)
            return o;

        // u 允许进入
        if (Wn.ROLE.ADMIN == role && ((md >> 6) & mask) == mask)
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
