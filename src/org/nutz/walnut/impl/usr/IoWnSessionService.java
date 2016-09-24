package org.nutz.walnut.impl.usr;

import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.io.WnEvalLink;
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

    /**
     * @param u
     *            用户
     * @param pseid
     *            父会话ID，null 表示顶级会话
     * @param du
     *            持续时间，小于0 将采用默认
     * @return 新创建的会话对象
     */
    private WnSession __create(WnUsr u, String pseid, long du) {
        // 创建一个 Session 对象
        WnObj o = io.create(oSessions, "${id}", WnRace.FILE);
        // io.changeType(o, SESSTP);

        // 小于 0 采用默认会话持续时间
        if (du < 0)
            du = this.duration;

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
        o.expireTime(o.lastModified() + du);
        o.setv("du", du);
        o.setv("me", u.name());
        o.setv("grp", u.mainGroup());
        if (!Strings.isBlank(pseid)) {
            o.setv("p_se_id", pseid);
        }

        io.set(o, "^(tp|g|mime|expi|du|me|grp|p_se_id)$");

        // 创建 Session 对象
        WnSession se = new IoWnSession(io, o);

        // 计入 session 并持久化
        se.vars().putAll(envs);
        se.save();

        return se;
    }

    @Override
    public WnSession login(final String nm, final String pwd, long du) {
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

        return create(u, du);
    }

    @Override
    public WnSession login(String nm, String pwd) {
        return login(nm, pwd, -1);
    }

    @Override
    public WnSession create(WnUsr u) {
        return create(u, -1);
    }

    @Override
    public WnSession create(WnUsr u, long du) {
        return this.__create(u, null, du);
    }

    @Override
    public WnSession create(WnSession pse, WnUsr u) {
        return create(pse, u, -1);
    }

    @Override
    public WnSession create(WnSession pse, WnUsr u, long du) {
        return this.__create(u, pse.id(), du);
    }

    @Override
    public WnSession logout(String seid) {
        IoWnSession se = (IoWnSession) this.fetch(seid);
        IoWnSession re = null;
        if (null != se) {
            // 得到当前会话的文件对象
            WnObj oSe = se.getObj();

            // 试图返回父会话
            re = (IoWnSession) this.fetch(se.getParentSessionId());

            // 如果父会话存在，更新一下，同时延迟 10秒删除
            if (null != re) {
                this.__touch(re);
                oSe.expireTime(System.currentTimeMillis() + 10000L);
                oSe.setv("du", 0); // 标识了 du==0，那么即使 touch 也不会更新会话的过期时间了
                io.set(oSe, "^(expi|du)$");
                if (log.isDebugEnabled())
                    log.debugf("sess[%s] logout delay 10s", seid);
            }
            // 否则直接删了
            else {
                io.delete(oSe, true);
                if (log.isDebugEnabled())
                    log.debugf("sess[%s] logout", seid);
            }

        } else {
            if (log.isWarnEnabled())
                log.warnf("sess[%s] noexists", seid);
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
            Wn.WC().security(new WnEvalLink(io), () -> {
                io.delete(o);
            });
            return null;
        }

        return new IoWnSession(io, o);
    }

    @Override
    public WnSession check(String seid, boolean autoTouch) {
        WnObj o = this.__check_seobj(seid);
        IoWnSession se = new IoWnSession(io, o);
        if (autoTouch)
            this.__touch(se);
        return se;
    }

    private void __touch(IoWnSession se) {
        WnObj o = se.getObj();
        long du = se.duration();

        // 只有 duration 大于 0 才表示有效
        if (du > 0) {
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
