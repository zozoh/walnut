package org.nutz.walnut.api.usr;

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

public class WnSessionService {

    private static final Log log = Logs.get();

    private static final String SESSTP = "sess";

    private WnIo io;

    private int duration;

    private WnUsrService usrs;

    private WnObj oSessions;

    public WnUsrService usrs() {
        return usrs;
    }

    public void on_create() {
        oSessions = io.createIfNoExists(null, "/session", WnRace.DIR);
    }

    /**
     * 创建一个新的会话
     * 
     * @param nm
     *            用户名
     * @param pwd
     *            密码
     * @return 会话对象
     * @throws <ul>
     *         <li>"e.usr.noexists"
     *         <li>"e.usr.blank.pwd"
     *         <li>"e.usr.invalid.login"
     *         </ul>
     */
    public WnSession login(final String nm, final String pwd) {
        if (Strings.isBlank(pwd))
            throw Er.create("e.usr.blank.pwd");

        WnUsr u = usrs.check(nm);
        if (!u.password().equals(pwd)) {
            throw Er.create("e.usr.invalid.login");
        }

        return create(u, true);
    }

    /**
     * 为某用户直接创建一个 Session
     * 
     * @param u
     *            用户
     * @param reuse
     *            是否重用当前会话。即当前会话如果与用户重名，直接重用
     * @return 会话对象
     */
    public WnSession create(final WnUsr u, final boolean reuse) {
        // 创建一个 Session 对象
        WnObj o = io.create(null, "/session/${id}", WnRace.OBJ);
        io.changeType(o, SESSTP);

        // 设置环境变量等 ..
        WnSession se = new WnSession();
        se.id(o.id()).me(u.name());

        NutMap envs = new NutMap();
        for (Map.Entry<String, Object> en : u.entrySet()) {
            String key = en.getKey();
            // 不显示密码
            if ("passwd".equals(key)) {
                continue;
            }
            // HOME 特殊处理
            if ("home".equals(key)) {
                envs.setv("PWD", en.getValue());
                envs.setv("HOME", en.getValue());
            }
            // 其他加前缀
            else {
                envs.setv("MY_" + key.toUpperCase(), en.getValue());
            }
        }

        se.envs(envs);

        // 持久化
        io.writeJson(o, se, null);

        // 更新索引
        o.setv("du", duration);
        o.expireTime(o.lastModified() + duration);
        o.setv("me", u.name());
        io.set(o, "^me|expi$");

        return se;
    }

    /**
     * 获取会话的索引对象
     * 
     * @param seid
     *            会话 ID
     * @return 会话对应的对象
     */
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

    /**
     * 销毁一个会话对象，如果会话对象有父，则将父会话的 lm 更新
     * 
     * @param seid
     *            会话对象ID
     * 
     * @return null 表示当前用户没有 session 了，否则返回父 session 对象
     */
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

    private void __update(WnSession se) {
        // 确保 Session 对象
        WnObj o = _check_seobj(se.id());

        // 持久化
        io.writeJson(o, se, null);

        // 更新过期时间
        _touch(o);
    }

    private void _touch(WnObj o) {
        int du = o.getInt("du", duration);
        o.nanoStamp(System.nanoTime());
        o.expireTime(o.lastModified() + du);
        io.set(o, "^nano|lm|expi$");
    }

    public NutMap setEnvs(String seid, NutMap map) {
        WnSession se = this.check(seid);
        se.envs().putAll(map);
        __update(se);
        return se.envs();
    }

    public NutMap setEnv(String seid, String nm, String val) {
        return setEnvs(seid, Lang.map(nm, val));
    }

    public NutMap removeEnv(String seid, String... nms) {
        WnSession se = this.check(seid);
        for (String nm : nms) {
            se.envs().remove(nm);
        }
        __update(se);
        return se.envs();
    }

    public void touch(String seid) {
        WnObj o = this._check_seobj(seid);
        _touch(o);
    }

    /**
     * 根据 ID 获取一个会话对象
     * 
     * @param seid
     *            会话的 ID
     * @return 会话对象
     */
    public WnSession fetch(String seid) {
        WnObj o = this._fetch_seobj(seid);
        if (null == o)
            return null;

        // 如果过期，删除
        if (o.isExpired()) {
            io.delete(o);
            return null;
        }

        WnSession se = io.readJson(o, WnSession.class);
        return se;
    }

    /**
     * 根据 ID 获取一个会话对象。如果会话对象不存在或者过期，则统统抛错
     * 
     * @param seid
     *            会话的 ID
     * @return 会话对象
     * @throws <ul>
     *         <li>"e.session.noexists"
     *         <li>"e.session.expired"
     *         </ul>
     */
    public WnSession check(String seid) {
        WnSession se = fetch(seid);
        if (null == se) {
            throw Er.create("e.sess.noexists", seid);
        }
        touch(seid);
        return se;
    }

}
