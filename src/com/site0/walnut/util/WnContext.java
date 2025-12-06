package com.site0.walnut.util;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;

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
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnUser;

/**
 * 这个是 ThreadLocal 的上下文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnContext extends NutMap {

    private String ticket;

    private WnSession _session;

    private WnUser _me;

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
                                    WnUser usr = hc.login().checkUser(runby);
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

    public void setIPv4(String ipv4) {
        this.put("ipv4", ipv4);
    }

    public String getIPv4() {
        return this.getString("ipv4");
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

    public void suCore(WnUser user, WnSecurity secu, WnHookContext hc, Atom atom) {
        // 记录旧的值
        WnUser old_me = this._me;
        WnSecurity old_secu = this.security;
        WnHookContext old_hc = this.hookContext;
        // 执行
        try {
            this._me = user;
            this.security = secu;
            this.hookContext = hc;
            atom.run();
        }
        // 还原
        finally {
            this.security = old_secu;
            this.hookContext = old_hc;
            this._me = old_me;
        }
    }

    public <T> T suCore(WnUser user, WnSecurity secu, WnHookContext hc, Proton<T> proton) {
        suCore(user, secu, hc, (Atom) proton);
        return proton.get();
    }

    public void suCoreNoSecurity(WnIo io, WnUser user, Atom atom) {
        suCore(user, new WnEvalLink(io), null, atom);
    }

    public <T> T suCoreNoSecurity(WnIo io, WnUser user, Proton<T> proton) {
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

    public <T> T su(WnUser u, Proton<T> proton) {
        WnSession old_se = _session;
        WnUser old_u = _me;

        // 不用切换用户
        if (null != old_u && old_u.isSame(u)) {
            proton.run();
            return proton.get();
        }

        // 需要切换用户
        try {
            this._me = u;
            // 会话也不等，那么暂时先移除
            if (null != old_se && !old_se.getUser().isSame(u)) {
                this._session = null;
            }
            // 执行业务逻辑
            proton.run();
            return proton.get();
        }
        finally {
            // 恢复之前上下文会话
            this._me = old_u;
            this._session = old_se;
        }

    }

    public void su(WnUser u, Atom atom) {
        WnSession old_se = _session;
        WnUser old_u = this.getMe();

        // 不用切换用户
        if (null != old_u && old_u.isSame(u)) {
            atom.run();
            return;
        }

        // 需要切换用户
        try {
            this._me = u;
            // 会话也不等，那么暂时先移除
            if (null != old_se && !old_se.getUser().isSame(u)) {
                this._session = null;
            }
            // 执行业务逻辑
            atom.run();
        }
        finally {
            // 恢复之前上下文会话
            this._me = old_u;
            this._session = old_se;
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

    public WnUser getMe() {
        if (null != this._me) {
            return this._me;
        }
        if (null != this._session) {
            return this._session.getUser();
        }
        return null;
    }

    public WnUser checkMe() {
        WnUser me = this.getMe();
        if (null == me) {
            throw Er.create("e.wc.null.me");
        }
        return me;
    }

    public void setMe(WnUser me) {
        this._me = me;
    }

    public String getMyId() {
        WnUser me = this.getMe();
        if (null != me) {
            return me.getId();
        }
        return null;
    }

    public String checkMyId() {
        return this.checkMe().getId();
    }

    public String getMyName() {
        WnUser me = this.getMe();
        if (null != me) {
            return me.getName();
        }
        return null;
    }

    public String checkMyName() {
        return this.checkMe().getName();
    }

    public String getMyGroup() {
        WnUser me = this.getMe();
        if (null != me) {
            return me.getMainGroup();
        }
        return null;
    }

    public String checkMyGroup() {
        return this.checkMe().getMainGroup();
    }

    public WnSession getSession() {
        // 防守一下，怕有其他的线程污染了这个上下文
        if (null != _session) {
            if (!_session.isSameTicket(ticket)) {
                return null;
            }
        }
        // 返回
        return _session;
    }

    public WnSession checkSession() {
        return checkSession(null);
    }

    public WnSession checkSession(WnLoginApi auth) {
        // 必须有 Session ID
        if (null == ticket) {
            throw Er.create("e.wc.null.ticket");
        }
        // 必须有 Session 对象
        if (null == _session) {
            // 如果没有，那么获取一个
            if (null != auth) {
                _session = auth.checkSession(ticket);
            }
            // 尽量通过上下文的账号伪造一个 Session
            else if (null != this._me) {
                WnSimpleSession se = new WnSimpleSession();
                se.setTicket("fake_session_" + R.UU64());
                se.setType("Fake");
                long now = System.currentTimeMillis();
                se.setCreateTime(now);
                se.setLastModified(now);
                se.setDuration(10);
                se.setExpiAt(now + 86400000L);
                se.setUser(_me);
                return se;
            }
            // 没戏了，抛错吧
            else {
                throw Er.create("e.wc.null.se");
            }
        }
        // SessionID 与对象必须匹配
        if (!_session.isSameTicket(ticket)) {
            throw Er.create("e.wc.ticket.nomatch");
        }
        // 嗯，揍是介个
        return _session;
    }

    public void setSession(WnSession se) {
        this._session = se;
        // 设置
        if (null != se) {
            this.ticket = se.getTicket();
            this._me = se.getUser();
            TimeZone tz = Wtime.getSessionTimeZone(se);
            this.setTimeZone(tz);
        }
        // 删除
        else {
            this.ticket = null;
            if (null != this._me
                && null != this._session
                && this._me.isSame(this._session.getUser())) {
                this._me = null;
            }
            this.setTimeZone(null);
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
                if (Wlang.contains(cookieNames, cknm)) {
                    if (Wn.AT_SEID.equals(cknm)) {
                        this.ticket = co.getValue();
                    } else {
                        this.setv(cknm, co.getValue());
                    }
                }
            }
    }

}
