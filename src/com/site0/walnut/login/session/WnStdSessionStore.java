package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.login.usr.WnLazyUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnUserStore;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.WnConfig;

public class WnStdSessionStore extends AbstractWnSessionStore {

    private static final Log log = Wlog.getAUTH();

    private WnIo io;
    private WnObj oHome;

    public WnStdSessionStore(WnIo io, NutBean sessionVars, String homePath, NutMap defaultEnv) {
        this.io = io;
        this.defaultEnv = defaultEnv;

        // 获取会话主目录
        homePath = Ws.sBlank(homePath, "~/.domain/session");
        this.oHome = Wn.checkObj(io, sessionVars, homePath);
    }

    /**
     * 为系统用户会话准备的构造器
     * 
     * @param io
     */
    public WnStdSessionStore(WnIo io, WnConfig conf) {
        this.io = io;
        this.defaultEnv = conf.getSessionDefaultEnv();

        // 获取会话主目录
        this.oHome = io.check(null, "/var/session");
    }

    @Override
    public WnSession getSession(String ticket, WnUserStore users) {
        WnObj obj = io.fetch(oHome, ticket);
        // 防空
        if (null == obj) {
            return null;
        }

        WnSimpleSession se = new WnSimpleSession();
        // 设置标识
        se.setTicket(obj.name());
        se.setParentTicket(obj.getString("parent_ticket"));

        // 过期时间
        se.setExpiAt(obj.expireTime());

        // 加载用户
        String uid = obj.getString("u_id");
        if (Ws.isBlank(uid)) {
            log.warnf("session without u_id, ticket=%s", ticket);
        }
        // 读取用户
        else {
            WnLazyUser u = new WnLazyUser(users);
            u.setInnerUser(users.getUserRace(),
                           uid,
                           obj.getString("u_name"),
                           obj.getString("email"),
                           obj.getString("phone"));
            se.setUser(u);
        }

        // 设置环境变量
        se.setEnv(obj.getAs("env", NutMap.class));

        // 返回结果
        return se;
    }

    @Override
    public void addSession(WnSession se) {
        // 获取用户
        WnUser u = se.getUser();

        // 转换为一个 Bean
        WnObj bean = new WnIoObj();
        bean.name(se.getTicket());
        bean.expireTime(se.getExpiAt());
        bean.put("u_id", u.getId());
        bean.put("u_name", u.getName());
        bean.put("email", u.getEmail());
        bean.put("phone", u.getPhone());
        bean.put("env", se.getEnv());
        if (se.hasParentTicket()) {
            bean.put("parent_ticket", se.getParentTicket());
        }

        // 保存
        WnObj oSe = io.create(oHome, bean);
        se.setCreateTime(oSe.createTime());
        se.setLastModified(oSe.lastModified());

    }

    protected void _remove_session(WnSession se) {
        WnObj obj = io.fetch(oHome, se.getTicket());
        // 防空
        if (null != obj) {
            io.delete(obj);
        }
    }

    @Override
    public void saveSessionEnv(WnSession se) {
        WnObj obj = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == obj) {
            return;
        }
        NutMap meta = Wlang.map("env", se.getEnv());
        io.appendMeta(obj, meta);
    }

    @Override
    public void touchSession(WnSession se, int duInSec) {
        WnObj obj = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == obj) {
            return;
        }
        se.setExpiAt(System.currentTimeMillis() + duInSec * 1000L);
        NutMap meta = Wlang.map("expi", se.getExpiAt());
        io.appendMeta(obj, meta);

    }

}
