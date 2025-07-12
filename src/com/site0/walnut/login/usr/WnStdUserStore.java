package com.site0.walnut.login.usr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class WnStdUserStore extends AbstractWnUserStore {

    private WnIo io;

    private WnObj oHome;

    public WnStdUserStore(UserRace userRace,
                          WnIo io,
                          NutBean sessionVars,
                          String homePath,
                          NutMap defaultMeta) {
        super(userRace, defaultMeta);
        this.io = io;
        homePath = Ws.sBlank(homePath, "~/.domain/session");
        this.oHome = Wn.checkObj(io, sessionVars, homePath);
    }

    /**
     * 为系统用户准备的构造器
     * 
     * @param io
     */
    public WnStdUserStore(WnIo io) {
        super(UserRace.SYS, null);
        // defaultMeta 会在Ioc构造字段的时候设置，它应该通过
        // {java: "$conf.xxx"} 去获取默认配置文件里的用户默认元数据
        this.io = io;
        this.oHome = io.check(null, "/sys/usr");
    }

    private NutMap _to_bean_for_update(WnUser u) {
        NutBean bean = u.toBean();
        NutMap re = new NutMap();
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            // 忽略
            if (key.matches("^(lastLoginAtInUTC|userRace)$")) {
                continue;
            }

            // 密码获取加密值
            if ("passwd".equals(key)) {
                re.put(key, u.getPasswd());
            }
            // 盐值
            else if ("salt".equals(key)) {
                re.put(key, u.getSalt());
            }
            // 转换为 snake
            else {
                re.put(key, val);
            }
        }
        return re;
    }

    @Override
    public WnUser addUser(WnUser u) {
        NutMap bean = _to_bean_for_update(u);
        WnIoObj obj = new WnIoObj();
        obj.update(bean);

        WnObj oU = io.create(oHome, obj);

        return toWnUser(oU);
    }

    @Override
    public void saveUserMeta(WnUser u) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenSaveUserMeta", u.toString());
        }
        NutMap delta = new NutMap();
        if (u.hasMeta()) {
            delta.putAll(u.getMeta());
        }
        delta.put("HOME", u.getHomePath());
        io.appendMeta(oU, delta);

    }

    @Override
    public void updateUserName(WnUser u) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenUpdateUserName", u.toString());
        }
        NutMap delta = new NutMap();
        delta.put("nm", u.getName());

        io.appendMeta(oU, delta);
    }

    @Override
    public void updateUserPhone(WnUser u) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenUpdateUserPhone", u.toString());
        }
        NutMap delta = new NutMap();
        delta.put("phone", u.getPhone());

        io.appendMeta(oU, delta);
    }

    @Override
    public void updateUserEmail(WnUser u) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenUpdateUserEmail", u.toString());
        }
        NutMap delta = new NutMap();
        delta.put("email", u.getEmail());

        io.appendMeta(oU, delta);
    }

    @Override
    public void updateUserLastLoginAt(WnUser u) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenUpdateUserLastLoginAt", u.toString());
        }
        NutMap delta = new NutMap();
        delta.put("last_login_at", u.getLastLoginAt());

        io.appendMeta(oU, delta);
    }

    @Override
    public void updateUserPassword(WnUser u, String rawPassword) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenUpdateUserPassword", u.toString());
        }
        u.genSaltAndRawPasswd(rawPassword);
        NutMap delta = new NutMap();
        delta.put("salt", u.getSalt());
        delta.put("passwd", u.getPasswd());

        io.appendMeta(oU, delta);
    }

    @Override
    public List<WnUser> queryUser(WnQuery q) {
        // 默认
        if (null == q) {
            q = Wn.Q.pid(oHome);
        }
        // 强制设置 pid
        else {
            q.setv("pid", oHome.id());
        }
        if (!q.hasLimit()) {
            q.limit(100);
        }

        List<WnObj> objs = io.query(q);
        List<WnUser> list = new ArrayList<>(objs.size());
        for (WnObj obj : objs) {
            // 无视过期的对象
            if (obj.isExpired()) {
                continue;
            }
            list.add(toWnUser(obj));
        }

        return list;
    }

    @Override
    public WnUser getUserById(String uid) {
        WnObj oU = __get_user_obj(uid);
        if (null == oU) {
            return null;
        }
        if (!oHome.isSameId(oU.parentId())) {
            throw Er.create("e.auth.acc_outof_home", uid);
        }
        return toWnUser(oU);
    }

    private WnObj __get_user_obj(String uid) {
        // 考虑两段式 ID，确保起始段一定为 oAccountDir
        WnObjId oid = new WnObjId(uid);

        // 读取用户
        WnObj oU = io.getIn(oHome, oid.getMyId());
        return oU;
    }

}
