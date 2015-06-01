package org.nutz.walnut.util;

import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNodeCallback;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;

/**
 * 这个是 ThreadLocal 的上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnContext extends NutMap {

    private String seid;

    private WnSession se;

    private String me;

    private String grp;

    private WnSecurity security;

    private WnNodeCallback on_create;

    public WnSecurity getSecurity() {
        return security;
    }

    public void setSecurity(WnSecurity callback) {
        this.security = callback;
    }

    public WnNode whenEnter(WnNode nd) {
        if (null != security)
            return security.enter(nd);
        return nd;
    }

    public WnNode whenAccess(WnNode nd) {
        if (null != security)
            return security.access(nd);
        return nd;
    }

    public WnNode whenRead(WnNode nd) {
        if (null != security)
            return security.read(nd);
        return nd;
    }

    public WnNode whenWrite(WnNode nd) {
        if (null != security)
            return security.write(nd);
        return nd;
    }

    public WnNode whenView(WnNode nd) {
        if (null != security)
            return security.view(nd);
        return nd;
    }

    public WnNode whenCreate(WnNode nd) {
        if (null != on_create)
            return on_create.invoke(nd);
        return nd;
    }

    public void onCreate(WnNodeCallback callback) {
        this.on_create = callback;
    }

    public WnNodeCallback onCreate() {
        return on_create;
    }

    public String checkMe() {
        if (null == me) {
            throw Er.create("e.wc.null.me");
        }
        return me;
    }

    public String checkGroup() {
        if (null == grp) {
            throw Er.create("e.wc.null.grp");
        }
        return grp;
    }

    public void me(String me, String grp) {
        this.me = me;
        this.grp = grp;
    }

    public <T> T su(WnUsr u, Proton<T> proton) {
        String old_me = me;
        String old_grp = grp;
        try {
            me = u.name();
            grp = u.group();
            proton.run();
            return proton.get();
        }
        finally {
            me = old_me;
            grp = old_grp;
        }

    }

    public void su(WnUsr u, Atom atom) {
        String old_me = me;
        String old_grp = grp;
        try {
            me = u.name();
            grp = u.group();
            atom.run();
        }
        finally {
            me = old_me;
            grp = old_grp;
        }

    }

    public String SEID() {
        return seid;
    }

    public String checkSEID() {
        if (null == seid) {
            throw Er.create("e.wc.null.seid");
        }
        return seid;
    }

    public void SEID(String seid) {
        this.seid = seid;
    }

    public WnSession SE() {
        return se;
    }

    public void SE(WnSession se) {
        this.se = se;
        // 设置
        if (null != se) {
            this.seid = se.id();
            this.me = se.me();
            this.grp = se.group();
        }
        // 删除
        else {
            this.seid = null;
            this.me = null;
            this.grp = null;
        }
    }

    public WnSession checkSE() {
        if (null == seid) {
            throw Er.create("e.wc.null.seid");
        } else if (null == se) {
            throw Er.create("e.wc.null.se");
        } else if (!se.id().equals(seid)) {
            throw Er.create("e.wc.seid.nomatch");
        }
        return se;
    }

}
