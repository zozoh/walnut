package com.site0.walnut.login.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.login.usr.WnLazyUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.web.WnConfig;

public class WnStdSessionStore extends AbstractWnSessionStore {

    private static final Log log = Wlog.getAUTH();

    WnObj oHome;

    public WnStdSessionStore(WnIo io,
                             NutBean sessionVars,
                             String homePath,
                             NutMap defaultEnv) {
        super(io, sessionVars, defaultEnv);

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
        super(io, Wlang.map("HOME", "/root"), conf.getSessionDefaultEnv());

        // 获取会话主目录
        this.oHome = io.check(null, "/var/session");
    }

    @Override
    protected List<WnSession> _query(NutMap filter,
                                     NutMap sorter,
                                     int skip,
                                     int limit) {
        // 查询条件
        WnQuery q = Wn.Q.pid(oHome);
        q.setAll(filter);
        q.sort(sorter);
        q.limit(limit);

        // 执行查询
        List<WnObj> oSeList = io.query(q);

        // 循环处理数据
        List<WnSession> reList = new ArrayList<>(oSeList.size());
        for (WnObj oSe : oSeList) {
            WnSimpleSession se = __build_session_obj(oSe);
            reList.add(se);
        }

        // 返回
        return reList;
    }

    @Override
    public WnSession _get_one(String ticket) {
        WnObj oSe = io.fetch(oHome, ticket);

        // 创建会话对象
        WnSimpleSession se = __build_session_obj(oSe);

        // 返回结果
        return se;
    }

    @Override
    public WnSession _find_one_by_uid_type(String uid, String type) {
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("type", type);
        q.setv("u_id", uid);
        WnObj oSe = io.getOne(q);

        // 创建会话对象
        WnSimpleSession se = __build_session_obj(oSe);

        // 返回结果
        return se;
    }

    @Override
    public WnSession _find_one_by_unm_type(String unm, String type) {
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("type", type);
        q.setv("u_name", unm);
        WnObj oSe = io.getOne(q);

        // 创建会话对象
        WnSimpleSession se = __build_session_obj(oSe);

        // 返回结果
        return se;
    }

    protected WnSimpleSession __build_session_obj(WnObj oSe) {
        // 防空
        if (null == oSe) {
            return null;
        }

        WnSimpleSession se = new WnSimpleSession();
        // 设置标识
        se.setSite(oSe.getString("site"));
        se.setTicket(oSe.name());
        se.setType(oSe.getString("type"));
        se.setParentTicket(oSe.getString("parent_ticket"));
        se.setChildTicket(oSe.getString("child_ticket"));
        se.setDuration(oSe.getInt("duration"));

        // 加载用户
        String uid = oSe.getString("u_id");
        if (Ws.isBlank(uid)) {
            log.warnf("session without u_id, ticket=%s", se.getTicket());
        }
        // 设置用户
        else {
            WnLazyUser u = new WnLazyUser();
            String name = oSe.getString("u_name");
            String email = oSe.getString("email");
            String phone = oSe.getString("phone");
            u.setInnerUser(null, uid, name, email, phone);
            se.setUser(u);
        }

        // 时间戳
        se.setExpiAt(oSe.expireTime());
        se.setCreateTime(oSe.createTime());
        se.setLastModified(oSe.lastModified());

        // 设置环境变量
        se.setEnv(oSe.getAs("env", NutMap.class));
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
        bean.put("duration", se.getDuration());
        bean.put("type", se.getType());
        bean.put("u_id", u.getId());
        bean.put("u_name", u.getName());
        bean.put("email", u.getEmail());
        bean.put("phone", u.getPhone());
        bean.put("env", se.getEnv());
        bean.put("no_cached", true);
        if (se.hasParentTicket()) {
            bean.put("parent_ticket", se.getParentTicket());
        }
        if (se.hasChildTicket()) {
            bean.put("child_ticket", se.getChildTicket());
        }
        if (se.hasSite()) {
            bean.put("site", se.getSite());
        }

        // 保存
        WnObj oSe = io.create(oHome, bean);
        se.setCreateTime(oSe.createTime());
        se.setLastModified(oSe.lastModified());

    }

    @Override
    public void saveSessionEnv(WnSession se) {
        WnObj oSe = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == oSe) {
            return;
        }

        long now = System.currentTimeMillis();
        se.setExpiAt(now + se.getDurationInMs());

        NutBean delta = new NutMap();
        delta.put("env", se.getEnv());
        delta.put("lm", now);
        delta.put("expi", se.getExpiAt());
        io.appendMeta(oSe, delta);
    }

    @Override
    public void saveSessionChildTicket(WnSession se) {
        if (!se.hasChildTicket()) {
            return;
        }
        WnObj oSe = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == oSe) {
            return;
        }

        long now = System.currentTimeMillis();
        se.setExpiAt(now + se.getDurationInMs());

        NutBean delta = new NutMap();
        delta.put("child_ticket", se.getChildTicket());
        delta.put("lm", now);
        delta.put("expi", se.getExpiAt());
        io.appendMeta(oSe, delta);

    }

    @Override
    public void touchSession(WnSession se, int duInSec) {
        WnObj oSe = io.fetch(oHome, se.getTicket());
        // 防空
        if (null == oSe) {
            return;
        }
        long now = System.currentTimeMillis();
        se.setExpiAt(System.currentTimeMillis() + duInSec * 1000L);
        
        long expiAt = se.getExpiAt();
        Date d = new Date(expiAt);
        String str = Wtime.formatUTC(d, "yyyy-MM-dd HH:mm:ss.SSS");
        log.infof("== touchSession:setExpiAt: %s => %s", expiAt, str);

        NutBean delta = new NutMap();
        delta.put("expi", se.getExpiAt());
        delta.put("lm", now);
        io.appendMeta(oSe, delta);

    }

    protected void _remove_session(WnSession se) {
        WnObj obj = io.fetch(oHome, se.getTicket());
        // 防空
        if (null != obj) {
            io.delete(obj);
        }
    }

}
