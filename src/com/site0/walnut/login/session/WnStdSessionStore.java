package com.site0.walnut.login.session;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.login.WnUserStore;
import com.site0.walnut.login.usr.WnLazyUser;
import com.site0.walnut.login.usr.WnSessionStoreSetup;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class WnStdSessionStore extends AbstractWnSessionStore {

    private static final Log log = Wlog.getAUTH();

    private WnIo io;
    private NutBean sessionVars;
    private WnObj oHome;

    public WnStdSessionStore(WnSessionStoreSetup setup) {
        this.defaultEnv = setup.defaultEnv;
        this.io = setup.io;
        this.sessionVars = setup.sessionVars;

        // 获取会话主目录
        this.oHome = Wn.checkObj(io, sessionVars, setup.path);
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

        // 过期时间
        se.setExpiAt(obj.expireTime());

        // 设置环境变量
        se.setEnv(obj.getAs("env", NutMap.class));

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

        // 保存
        WnObj oSe = io.create(oHome, bean);
        se.setCreateTime(oSe.createTime());
        se.setLastModified(oSe.lastModified());

    }

    public boolean reomveSession(WnSession se) {
        WnObj obj = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == obj) {
            return false;
        }
        io.delete(obj);
        return true;
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
    public void touchSession(WnSession se, long sessionDuration) {
        WnObj obj = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == obj) {
            return;
        }
        se.setExpiAt(System.currentTimeMillis() + sessionDuration);
        NutMap meta = Wlang.map("expi", se.getExpiAt());
        io.appendMeta(obj, meta);

    }

}
