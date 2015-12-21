package org.nutz.walnut.impl.usr;

import java.util.Map;

import org.nutz.json.Json;
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

    private String sessionHome;

    private int duration;

    private WnUsrService usrs;

    private WnObj oSessions;

    public void on_create() {
        Wn.WC().me("root", "root");
        oSessions = io.createIfNoExists(null, sessionHome, WnRace.DIR);
    }

    @Override
    public WnUsrService usrs() {
        return usrs;
    }

    @Override
    public WnSession login(final String nm, final String pwd) {
        if (Strings.isBlank(pwd))
            throw Er.create("e.usr.blank.pwd");

        IoWnUsr u = usrs.check(nm);
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

    @Override
    public WnSession create(WnUsr u) {
        // 创建一个 Session 对象
        WnObj o = io.create(oSessions, "${id}", WnRace.FILE);
        // io.changeType(o, SESSTP);
        io.appendMeta(o, Lang.mapf("tp:'%s',mime:'%s'", SESSTP, "application/json"));

        // 设置环境变量等 ..
        WnSession se = new IoWnSession();
        se.id(o.id()).me(u);

        NutMap envs = new NutMap();
        for (Map.Entry<String, Object> en : u.entrySet()) {
            String key = en.getKey();
            // 不显示密码
            if ("passwd".equals(key)) {
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

        se.setEnvs(envs);
        se.var("PWD", u.home());

        // 持久化
        io.writeJson(o, se, null);

        // 更新索引
        o.setv("du", duration);
        o.expireTime(o.lastModified() + duration);
        o.setv("me", u.name());
        io.set(o, "^me|expi$");

        // if (log.isDebugEnabled()) {
        // log.debugf("CreateWnSessionObj, %s", Json.toJson(o));
        // log.debugf("CreateWnSession, %s", Json.toJson(se));
        // log.debugf("ExpireTime\nsysNano: %d\nsysCurr: %d\nwnsNano:
        // %d\nobjLaMo: %d",
        // System.nanoTime(),
        // System.currentTimeMillis(),
        // Wn.nanoTime(),
        // o.lastModified());
        // }

        return se;
    }

    private WnObj _fetch_seobj(String seid) {
        return io.fetch(oSessions, seid);
    }

    private WnObj _check_seobj(String seid) {
        WnObj o = _fetch_seobj(seid);
        if (null == o) {
            throw Er.create("e.sess.noexists", seid);
        }
        return o;
    }

    @Override
    public WnSession logout(String seid) {
        WnSession re = null;
        WnObj o = this._fetch_seobj(seid);
        if (null != o) {
            io.delete(o);
            if (log.isDebugEnabled())
                log.debugf("sess[%s] logout", seid);

        } else {
            if (log.isWarnEnabled())
                log.warnf("sess[%s] losed Obj", seid);
        }
        return re;
    }

    @Override
    public void save(WnSession se) {
        // 确保 Session 对象
        WnObj o = _check_seobj(se.id());

        // 持久化
        io.writeJson(o, se, null);

        // 更新过期时间
        _touch(o);
    }

    private void _touch(WnObj o) {
        int du = o.getInt("du", duration);
        o.lastModified(System.currentTimeMillis());
        o.expireTime(o.lastModified() + du);
        io.appendMeta(o, "^nano|lm|expi$");
    }

    @Override
    public void touch(String seid) {
        WnObj o = this._check_seobj(seid);
        _touch(o);
    }

    @Override
    public WnSession fetch(String seid) {
        final WnObj o = this._fetch_seobj(seid);
        if (null == o)
            return null;

        // 如果过期，删除
        if (o.isExpired()) {
            WnUsr root = usrs.check("root");
            Wn.WC().su(root, new Atom() {
                public void run() {
                    io.delete(o);
                }
            });
            return null;
        }

        String json = io.readText(o);
        return Json.fromJson(IoWnSession.class, json);
    }

    @Override
    public WnSession check(String seid) {
        WnSession se = fetch(seid);
        if (null == se) {
            throw Er.create("e.sess.noexists", seid);
        }
        touch(seid);
        return se;
    }

}
