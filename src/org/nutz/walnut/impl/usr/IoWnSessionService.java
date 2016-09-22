package org.nutz.walnut.impl.usr;

import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;

public class IoWnSessionService implements WnSessionService {

    private static final Log log = Logs.get();

    private static final String SESSTP = "sess";

    private WnIo io;

    private long duration;

    private WnUsrService usrs;

    private WnObj oSessions;

    public void on_create() {
        Wn.WC().me("root", "root");
        oSessions = io.createIfNoExists(null, "/sys/session", WnRace.DIR);
    }

    @Override
    public WnUsrService usrs() {
        return usrs;
    }

    @Override
    public WnSession login(final String nm, final String pwd) {
        if (Strings.isBlank(pwd))
            throw Er.create("e.usr.blank.pwd");

        WnUsr u = usrs.check(nm);
        if (!usrs.checkPassword(nm, pwd)) {
            throw Er.create("e.usr.invalid.login");
        }

        // 如果主目录过期，也不能登录
        WnObj oHome = io.check(null, u.home());
        if (oHome.isExpired()) {
            throw Er.create("e.usr.home.expired");
        }

        return create(u);
    }

    private WnSession __create(WnUsr u, String parentSeId) {
        // 创建一个 Session 对象
        WnObj o = io.create(oSessions, "${id}", WnRace.FILE);
        // io.changeType(o, SESSTP);

        // 准备环境变量
        NutMap envs = Lang.map("PWD", u.home());
        for (Map.Entry<String, Object> en : u.entrySet()) {
            String key = en.getKey();
            // 双下划线开始的元数据无视
            if (key.startsWith("__"))
                continue;
            // 其他不显示的键
            if (key.matches("^(passwd|salt|pid|ct|lm|data|sha1|len|c|g|m|d0|d1|md|tp|mime|ph)$")) {
                continue;
            }
            // HOME 特殊处理
            if ("home".equals(key)) {
                envs.setv("HOME", en.getValue());
            }
            // 如果是大写的变量，则全部保留，比如 "PATH" 或者 "APP-PATH"
            else if (key.toUpperCase().equals(key)) {
                envs.setv(key, en.getValue());
            }
            // 其他加前缀
            else {
                envs.setv("MY_" + key.toUpperCase(), en.getValue());
            }
        }

        // 更新文件元数据
        o.type(SESSTP);
        o.mime("application/json");
        o.group(u.mainGroup()); // 确保这个文件是属于当前用户主组的
        o.expireTime(o.lastModified() + duration);
        o.setv("du", duration);
        o.setv("me", u.name());
        o.setv("grp", u.mainGroup());

        io.set(o, "^(tp|g|mime|expi|du|me|grp)$");

        // 创建 Session 对象
        WnSession se = new IoWnSession(io, o);

        // 计入 session 并持久化
        se.vars().putAll(envs);
        se.save();

        return se;
    }

    @Override
    public WnSession create(WnUsr u) {
        return this.__create(u, null);
    }

    @Override
    public WnSession login(WnSession pse) {
        WnUsr u = usrs.check(pse.me());
        return this.__create(u, pse.id());
    }

    @Override
    public WnSession logout(String seid) {
        IoWnSession se = (IoWnSession) this.fetch(seid);
        IoWnSession re = null;
        if (null != se) {
            io.delete(se.getObj(), true);
            if (log.isDebugEnabled())
                log.debugf("sess[%s] logout", seid);

            // 试图返回父会话
            re = (IoWnSession) this.fetch(se.getParentSessionId());

            // 如果父会话存在，更新一下
            if (null != re)
                this.__touch(re);

        } else {
            if (log.isWarnEnabled())
                log.warnf("sess[%s] losed Obj", seid);
        }

        // 返回父会话
        return re;
    }

    @Override
    public void touch(String seid) {
        WnObj o = this.__check_seobj(seid);
        IoWnSession se = new IoWnSession(io, o);
        this.__touch(se);
    }

    @Override
    public WnSession fetch(String seid) {
        final WnObj o = this.__fetch_seobj(seid);
        if (null == o)
            return null;

        // 如果过期，删除
        if (o.isExpired()) {
            WnUsr root = usrs.check("root");
            Wn.WC().su(root, new Atom() {
                public void run() {
                    logout(seid);
                }
            });
            return null;
        }

        return new IoWnSession(io, o);
    }

    @Override
    public WnSession check(String seid) {
        WnObj o = this.__check_seobj(seid);
        IoWnSession se = new IoWnSession(io, o);
        this.__touch(se);
        return se;
    }

    private void __touch(IoWnSession se) {
        WnObj o = se.getObj();
        long du = se.duration();
        o.lastModified(System.currentTimeMillis());
        o.expireTime(o.lastModified() + du);
        io.appendMeta(o, "^(lm|expi)$");

        // 试图更新父会话
        if (se.hasParentSession()) {
            IoWnSession pse = (IoWnSession) this.fetch(se.getParentSessionId());
            if (null != pse)
                this.__touch(pse);
        }
    }

    private WnObj __fetch_seobj(String seid) {
        if (null == seid)
            return null;
        return io.fetch(oSessions, seid);
    }

    private WnObj __check_seobj(String seid) {
        WnObj o = __fetch_seobj(seid);
        if (null == o) {
            throw Er.create("e.sess.noexists", seid);
        }
        return o;
    }
}
