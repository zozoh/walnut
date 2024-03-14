package com.site0.walnut.util;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHook;
import com.site0.walnut.api.hook.WnHookBreak;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.impl.io.WnSecurityImpl;

/**
 * 这个是 ThreadLocal 的上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnContext extends NutMap {

    private String ticket;

    private WnAuthSession session;

    private WnAccount account;

    private WnSecurity security;

    private boolean synctime_off;

    private WnHookContext hookContext;

    private TimeZone timeZone;

    public long _timestamp;

    /**
     * query|each 的时候，是否自动加载全路径，默认应该是 false
     */
    private boolean autoPath;

    public boolean isAutoPath() {
        return autoPath;
    }

    public void autoPath(boolean autoPath, Atom atom) {
        boolean oldAutoPath = this.autoPath;
        try {
            this.autoPath = autoPath;
            atom.run();
        }
        finally {
            this.autoPath = oldAutoPath;
        }
    }

    public <T> T autoPath(boolean autoPath, Proton<T> proton) {
        boolean oldAutoPath = this.autoPath;
        try {
            this.autoPath = autoPath;
            proton.run();
            return proton.get();
        }
        finally {
            this.autoPath = oldAutoPath;
        }
    }

    public List<WnHook> getHooks(String action, WnObj o) {
        if (null != hookContext) {
            return hookContext.service.get(action, o);
        }
        return Collections.emptyList();
    }

    private static final Log hookLog = Wlog.getHOOK();

    public WnObj doHook(String action, WnObj o) {
        if (null == o)
            return null;
        if (null != hookContext) {
            Stopwatch sw = Stopwatch.begin();

            WnHookService srv = hookContext.service;
            if (hookLog.isDebugEnabled()) {
                hookLog.debugf("doHook<%s> for'%s' by: %s", action, o.name(), srv.toString());
            }
            List<WnHook> hooks = srv.get(action, o);
            if (null != hooks && hooks.size() > 0) {
                if (hookLog.isInfoEnabled())
                    hookLog.infof(" - HOOK(%d)%s:BEGIN:%s", hooks.size(), action, o.path());

                // 为了防止无穷递归，钩子里不再触发钩子
                WnHookContext hc = hookContext;
                hookContext = null;
                try {
                    int i = 0;
                    for (WnHook hook : hooks) {
                        if (hookLog.isInfoEnabled())
                            hookLog.infof(" @[%d]: %s", i++, hook);

                        try {
                            String runby = hook.getRunby();
                            // 保持当前线程的用户
                            if (Strings.isBlank(runby)) {
                                hook.invoke(hc, o);
                            }
                            // 切换用户
                            else {
                                this.security(new WnEvalLink(hc.io()), () -> {
                                    WnAccount usr = hc.auth().checkAccount(runby);
                                    this.su(usr, new Atom() {
                                        public void run() {
                                            hook.invoke(hc, o);
                                        }
                                    });
                                });
                            }
                        }
                        catch (WnHookBreak e) {
                            if (hookLog.isDebugEnabled())
                                hookLog.debug(" ! break !");
                            break;
                        }
                        catch (Exception e) {
                            if (hookLog.isWarnEnabled())
                                hookLog.warn(" !! hook wrong", e);
                        }
                    }
                }
                // 恢复钩子上下文
                finally {
                    hookContext = hc;
                }

                sw.stop();
                if (hookLog.isInfoEnabled())
                    hookLog.infof(" - HOOK(%d)%s: DONE:%dms",
                                  hooks.size(),
                                  action,
                                  sw.getDuration());

                // 调用了钩子，则重新获取
                return hookContext.io().get(o.id());
            }

        }
        // 木有钩子，是不符合预期的
        // （为单元测试开的一个支线逻辑）
        else if (o.getBoolean("__debug_hook")) {
            hookLog.warnf("%s : %s ! without find hooks !", action, o.toString());
        }
        // 没有调用钩子，返回自身
        return o;
    }

    public WnHookContext getHookContext() {
        return hookContext;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
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
        hooking(hc, (Atom) proton);
        return proton.get();
    }

    public void setSynctimeOff(boolean so) {
        this.synctime_off = so;
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
        synctimeOff((Atom) proton);
        return proton.get();
    }

    public boolean isSecurityNoCheck() {
        if (null == security)
            return true;
        if (security.getClass().equals(WnSecurityImpl.class))
            return false;
        return true;
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
        security(secu, (Atom) proton);
        return proton.get();
    }

    public void nosecurity(WnIo io, Atom atom) {
        Wn.WC().security(new WnEvalLink(io), atom);
    }

    public <T> T nosecurity(WnIo io, Proton<T> proton) {
        security(new WnEvalLink(io), (Atom) proton);
        return proton.get();
    }

    public void core(WnSecurity secu, boolean synctimeOff, WnHookContext hc, Atom atom) {
        final WnContext wc = this;

        wc.security(secu, () -> {
            if (synctimeOff) {
                wc.synctimeOff(() -> {
                    wc.hooking(hc, atom);
                });
            } else {
                wc.hooking(hc, atom);
            }
        });
    }

    public <T> T core(WnSecurity secu, boolean synctimeOff, WnHookContext hc, Proton<T> proton) {
        core(secu, synctimeOff, hc, (Atom) proton);
        return proton.get();
    }

    public void suCore(WnAccount user, WnSecurity secu, WnHookContext hc, Atom atom) {
        // 记录旧的值
        WnAccount old_me = this.account;
        WnSecurity old_secu = this.security;
        WnHookContext old_hc = this.hookContext;
        // 执行
        try {
            this.account = user;
            this.security = secu;
            this.hookContext = hc;
            atom.run();
        }
        // 还原
        finally {
            this.security = old_secu;
            this.hookContext = old_hc;
            this.account = old_me;
        }
    }

    public <T> T suCore(WnAccount user, WnSecurity secu, WnHookContext hc, Proton<T> proton) {
        suCore(user, secu, hc, (Atom) proton);
        return proton.get();
    }

    public void suCoreNoSecurity(WnIo io, WnAccount user, Atom atom) {
        suCore(user, new WnEvalLink(io), null, atom);
    }

    public <T> T suCoreNoSecurity(WnIo io, WnAccount user, Proton<T> proton) {
        return suCore(user, new WnEvalLink(io), null, proton);
    }

    public WnObj whenEnter(WnObj nd, boolean asNull) {
        if (null != security)
            return security.enter(nd, asNull);
        return nd;
    }

    public WnObj whenAccess(WnObj nd, boolean asNull) {
        if (null != security)
            return security.access(nd, asNull);
        return nd;
    }

    public WnObj whenRead(WnObj nd, boolean asNull) {
        if (null != security)
            return security.read(nd, asNull);
        return nd;
    }

    public WnObj whenWrite(WnObj nd, boolean asNull) {
        if (null != security)
            return security.write(nd, asNull);
        return nd;
    }

    public WnObj whenRemove(WnObj nd, boolean asNull) {
        if (null != security)
            return security.remove(nd, asNull);
        return nd;
    }

    public WnObj whenMeta(WnObj nd, boolean asNull) {
        if (null != security)
            return security.meta(nd, asNull);
        return nd;
    }

    public boolean testSecurity(WnObj nd, int mode) {
        if (null != security)
            return security.test(nd, mode);
        return true;
    }

    public <T> T su(WnAccount u, Proton<T> proton) {
        WnAccount old_u = this.getMe();
        try {
            this.setMe(u);
            proton.run();
            return proton.get();
        }
        finally {
            this.setMe(old_u);
        }

    }

    public void su(WnAccount u, Atom atom) {
        WnAccount old_u = this.getMe();
        try {
            this.setMe(u);
            atom.run();
        }
        finally {
            this.setMe(old_u);
        }
    }

    public String getTicket() {
        return ticket;
    }

    public boolean hasTicket() {
        return !Strings.isBlank(this.ticket);
    }

    public String checkTicket() {
        if (null == ticket) {
            throw Er.create("e.wc.null.ticket");
        }
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public WnAccount getMe() {
        if (null != this.account) {
            return this.account;
        }
        if (null != this.session) {
            return this.session.getMe();
        }
        return null;
    }

    public WnAccount checkMe() {
        WnAccount me = this.getMe();
        if (null == me) {
            throw Er.create("e.wc.null.me");
        }
        return me;
    }

    public void setMe(WnAccount me) {
        this.account = me;
    }

    public String getMyId() {
        WnAccount me = this.getMe();
        if (null != me) {
            return me.getId();
        }
        return null;
    }

    public String checkMyId() {
        return this.checkMe().getId();
    }

    public String getMyName() {
        WnAccount me = this.getMe();
        if (null != me) {
            return me.getName();
        }
        return null;
    }

    public String checkMyName() {
        return this.checkMe().getName();
    }

    public String getMyGroup() {
        WnAccount me = this.getMe();
        if (null != me) {
            return me.getGroupName();
        }
        return null;
    }

    public String checkMyGroup() {
        return this.checkMe().getGroupName();
    }

    public WnAuthSession getSession() {
        // 防守一下，怕有其他的线程污染了这个上下文
        if (null != session) {
            if (!session.isSameId(ticket))
                return null;
        }
        // 返回
        return session;
    }

    public WnAuthSession checkSession() {
        return checkSession(null);
    }

    public WnAuthSession checkSession(WnAuthService auth) {
        // 必须有 Session ID
        if (null == ticket) {
            throw Er.create("e.wc.null.ticket");
        }
        // 必须有 Session 对象
        if (null == session) {
            // 如果没有，那么获取一个
            if (null != auth) {
                session = auth.checkSession(ticket);
            }
            // 没戏了，抛错吧
            else {
                throw Er.create("e.wc.null.se");
            }
        }
        // SessionID 与对象必须匹配
        if (!session.isSameTicket(ticket)) {
            throw Er.create("e.wc.ticket.nomatch");
        }
        // 嗯，揍是介个
        return session;
    }

    public void setSession(WnAuthSession se) {
        this.session = se;
        // 设置
        if (null != se) {
            this.ticket = se.getTicket();
            this.account = se.getMe();
        }
        // 删除
        else {
            this.ticket = null;
            if (null != this.account
                && null != this.session
                && this.account.isSame(this.session.getMe())) {
                this.account = null;
            }
        }
    }

    /**
     * 如果请求对象的 Cookie 里存在制定的项目，copy 到本上下文中
     * 
     * @param req
     *            请求对象
     * @param cookieNames
     *            Cookie 名称列表
     */
    public void copyCookieItems(HttpServletRequest req, String[] cookieNames) {
        // 从 cookie 获得 SEID
        Cookie[] cookies = req.getCookies();
        if (null != cookies)
            for (Cookie co : cookies) {
                String cknm = co.getName();
                if (Lang.contains(cookieNames, cknm)) {
                    if (Wn.AT_SEID.equals(cknm)) {
                        this.ticket = co.getValue();
                    } else {
                        this.setv(cknm, co.getValue());
                    }
                }
            }
    }

}
