package org.nutz.walnut.ext.data.www;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class WWWAPI {

    /**
     * 指明默认 session 存放在何处。它下面会根据站点的 ID 为每个用户维护最多一个会话
     */
    protected String sessionHomePath;

    /**
     * IO 接口
     */
    protected WnIo io;

    /**
     * 当前用户操作的主目录，通常为 `/home/xxx`
     */
    protected WnObj oHome;

    protected long sessionDu;

    /**
     * 内部缓存一下会话主目录
     */
    protected WnObj __o_session_home;

    public WWWAPI(WnIo io, WnObj oHome, long sessionDu) {
        this.io = io;
        this.oHome = oHome;
        this.sessionDu = sessionDu;
        this.sessionHomePath = ".www/session";
    }

    public WnObj createSessionObj(String siteId, WnObj oU) {
        // 得到会话主目录
        String path = Wn.appendPath(this.sessionHomePath, siteId);
        WnObj oSset = io.createIfNoExists(oHome, path, WnRace.DIR);

        // 看看是否已经存在了这个用户，如果存在那么删掉这个会会话
        WnQuery q = Wn.Q.pid(oSset);
        q.setv("uid", oU.id());
        List<WnObj> oSothers = io.query(q);
        for (WnObj oSother : oSothers) {
            io.delete(oSother);
        }

        // 嗯嗯，登陆，创建会话
        String ticket = R.UU64(); // 准备一个票据
        WnObj oSe = io.create(oSset, ticket, WnRace.FILE);

        // 更新会话
        NutMap map = new NutMap();
        map.put("expi", Wn.now() + (sessionDu * 1000));
        map.put("uid", oU.id());
        map.put("unm", oU.name());
        map.put("sid", siteId);
        io.appendMeta(oSe, map);

        // 修改用户的最后登录时间
        oU.setv("login", oSe.lastModified());
        io.set(oU, "^login$");

        // 准备返回对象
        return oSe;
    }

    public NutBean createSession(String siteId, WnObj oU) {
        WnObj oSe = this.createSessionObj(siteId, oU);

        // 准备返回对象
        return this.genSessionMap(oSe);
    }

    public void chownSession(WnObj oSe, WnObj oU) {
        oSe.put("uid", oU.id());
        oSe.put("unm", oU.name());
        oSe.expireTime(Wn.now() + (sessionDu * 1000));
        io.set(oSe, "^(uid|unm|expi)$");
    }

    public WnObj fetchSessionObj(String siteId, WnObj oU) {
        // 得到会话主目录
        String path = Wn.appendPath(this.sessionHomePath, siteId);
        WnObj oSset = io.fetch(oHome, path);

        // 根本木有 Session 主目录
        if (null == oSset)
            return null;

        // 看看是否已经存在了这个用户，如果存在那么删掉这个会会话
        WnQuery q = Wn.Q.pid(oSset);
        q.setv("uid", oU.id());
        return io.getOne(q);
    }

    public NutBean fetchSession(String siteId, WnObj oU) {
        WnObj oSe = this.fetchSessionObj(siteId, oU);

        if (null != oSe)
            return this.genSessionMap(oSe);

        return null;
    }

    public WnObj getSessionObj(String siteId, String ticket) {
        if (Strings.isBlank(siteId) || Strings.isBlank(ticket))
            return null;
        // 得到会话主目录
        String path = Wn.appendPath(this.sessionHomePath, siteId);
        WnObj oSset = io.fetch(oHome, path);

        // 根本木有 Session 主目录
        if (null == oSset)
            return null;

        // 看看是否已经存在了这个用户，如果存在那么删掉这个会会话
        return io.fetch(oSset, ticket);
    }

    public NutBean getSession(String siteId, String ticket) {
        // 看看是否已经存在了这个用户，如果存在那么删掉这个会会话
        WnObj oSe = this.getSessionObj(siteId, ticket);

        if (null != oSe)
            return this.genSessionMap(oSe);

        return null;
    }

    protected NutBean genSessionMap(WnObj oSe) {
        return oSe.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|_.*)$");
    }

    protected NutBean genUserMap(WnObj oMe) {
        return oMe.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|passwd|salt|th_live|th_set.*|_.*)$");
    }

    protected NutBean genRoleMap(WnObj oRole) {
        return oRole.pickBy("^(nm|th_nm|mainpage|isdft)$");
    }

    protected WnObj getSessionHome() {
        if (null == this.__o_session_home) {
            this.__o_session_home = io.createIfNoExists(oHome, this.sessionHomePath, WnRace.DIR);
        }
        if (null == this.__o_session_home)
            throw Er.create("e.www.page.session_home_fail");
        return this.__o_session_home;
    }
}
