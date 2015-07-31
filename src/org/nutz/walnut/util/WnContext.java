package org.nutz.walnut.util;

import java.util.List;

import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHook;
import org.nutz.walnut.api.hook.WnHookBreak;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;

/**
 * 这个是 ThreadLocal 的上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnContext extends NutMap {

    private static final Log log = Logs.get();

    private String seid;

    private WnSession se;

    private String me;

    private String grp;

    private WnSecurity security;

    private boolean synctime_off;

    private WnHookContext hookContext;

    public long _timestamp;

    public WnObj doHook(String action, WnObj o) {
        if (null != hookContext) {
            Stopwatch sw = null;
            if (log.isInfoEnabled()) {
                if (log.isDebugEnabled())
                    sw = Stopwatch.begin();
                log.infof("HOOK@%s:BEGIN:%s", action, o.path());
            }

            List<WnHook> hooks = hookContext.service.get(action, o);
            if (null != hooks && hooks.size() > 0) {
                if (log.isDebugEnabled())
                    log.debugf(" - found %d hooks", hooks.size());

                // 为了防止无穷递归，钩子里不再触发钩子
                WnHookContext hc = hookContext;
                hookContext = null;
                try {
                    int i = 0;
                    for (WnHook hook : hooks) {
                        if (log.isDebugEnabled())
                            log.debugf(" @[%d]: %s", i++, hook.getClass().getSimpleName());

                        try {
                            hook.invoke(hc, o);
                        }
                        catch (WnHookBreak e) {
                            if (log.isDebugEnabled())
                                log.debug(" ! break !");
                            break;
                        }
                        catch (Exception e) {
                            if (log.isWarnEnabled())
                                log.warn(" !! hook wrong", e);
                        }
                    }
                }
                // 恢复钩子上下文
                finally {
                    hookContext = hc;
                }

                sw.stop();
                if (log.isDebugEnabled())
                    log.debugf("HOOK@%s: DONE %dms", action, sw.getDuration());

                // 调用了钩子，则重新获取
                return hookContext.io.checkById(o.id());
            }

            // 没有调用钩子，也记录一下时间
            if (log.isDebugEnabled()) {
                sw.stop();
                log.debugf("HOOK@%s: DONE:%dms:%s", action, sw.getDuration(), o.path());
            }
        }
        // 没有调用钩子，返回自身
        return o;
    }

    public WnHookContext getHookContext() {
        return hookContext;
    }

    public void setHookContext(WnHookContext hookContext) {
        this.hookContext = hookContext;
    }

    public void hooking(WnHookContext hc, Atom atom) {
        WnHookContext old = hookContext;
        try {
            hookContext = hc;
            atom.run();
        }
        finally {
            hookContext = old;
        }
    }

    public <T> T hooking(WnHookContext hc, Proton<T> proton) {
        WnHookContext old = hookContext;
        try {
            hookContext = hc;
            proton.run();
            return proton.get();
        }
        finally {
            hookContext = old;
        }
    }

    public boolean isSynctimeOff() {
        return synctime_off;
    }

    public void synctimeOff(Atom atom) {
        try {
            synctime_off = true;
            atom.run();
        }
        finally {
            synctime_off = false;
        }
    }

    public <T> T synctimeOff(Proton<T> proton) {

        try {
            synctime_off = true;
            proton.run();
            return proton.get();
        }
        finally {
            synctime_off = false;
        }
    }

    public WnSecurity getSecurity() {
        return security;
    }

    public void setSecurity(WnSecurity callback) {
        this.security = callback;
    }

    public void security(WnSecurity secu, Atom atom) {
        WnSecurity old = security;
        try {
            security = secu;
            atom.run();
        }
        finally {
            security = old;
        }
    }

    public <T> T security(WnSecurity secu, Proton<T> proton) {
        WnSecurity old = security;
        try {
            security = secu;
            proton.run();
            return proton.get();
        }
        finally {
            security = old;
        }
    }

    public WnObj whenEnter(WnObj nd) {
        if (null != security)
            return security.enter(nd);
        return nd;
    }

    public WnObj whenAccess(WnObj nd) {
        if (null != security)
            return security.access(nd);
        return nd;
    }

    public WnObj whenRead(WnObj nd) {
        if (null != security)
            return security.read(nd);
        return nd;
    }

    public WnObj whenWrite(WnObj nd) {
        if (null != security)
            return security.write(nd);
        return nd;
    }

    public WnObj whenView(WnObj nd) {
        if (null != security)
            return security.view(nd);
        return nd;
    }

    public WnObj whenRemove(WnObj nd) {
        if (null != security)
            return security.remove(nd);
        return nd;
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
