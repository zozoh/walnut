package com.site0.walnut.login.usr;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wuu;

public class WnStdUserStore extends AbstractWnUserStore {

    private WnIo io;

    private WnObj oHome;

    public WnStdUserStore(WnUserStoreSetup options) {
        this.io = options.io;
        this.oHome = Wn.checkObj(io, options.sessionVars, options.path);
        this.defaultMeta = options.defaultMeta;
        this.userRace = options.userRace;
    }

    @Override
    public void addUser(WnUser u) {
        NutMap bean = u.toBean();
        WnIoObj obj = new WnIoObj();
        obj.update(bean);

        io.create(oHome, obj);
    }

    @Override
    public void saveUserMeta(WnUser u) {
        WnObj oU = __get_user_obj(u.getId());
        if (null == oU) {
            throw Er.create("e.auth.user.UserNoExistsWhenSaveUserMeta", u.toString());
        }
        NutMap delta = new NutMap();
        join_user_meta_to_bean(u, delta);

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
        String salt = Wuu.UU32();
        String passwd = Wn.genSaltPassword(rawPassword, salt);
        u.setSalt(salt);
        u.setPasswd(passwd);
        NutMap delta = new NutMap();
        delta.put("salt", salt);
        delta.put("passwd", passwd);

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
