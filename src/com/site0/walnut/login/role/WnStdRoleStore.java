package com.site0.walnut.login.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.login.WnRole;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnRoleStore;
import com.site0.walnut.login.WnRoleType;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class WnStdRoleStore implements WnRoleStore {

    private WnIo io;

    private WnObj oHome;

    // TODO 这是个简单缓存，以后在扩展更优化的缓存策略
    private Map<String, WnRoleList> cache;

    public WnStdRoleStore(WnIo io, String homePath) {
        this.io = io;
        this.oHome = io.createIfNoExists(null, homePath, WnRace.DIR);
        this.cache = new HashMap<>();
    }

    @Override
    synchronized public WnRoleList getRoles(WnUser u) {
        return getRoles(u.getId());
    }

    @Override
    synchronized public WnRoleList getRoles(String uid) {
        // 尝试缓存
        WnRoleList re = cache.get(uid);
        if (null != re) {
            return re;
        }

        // 真正查询
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("uid", uid);
        q.sortBy("role", 1);
        List<WnObj> oRoles = io.query(q);
        List<WnRole> results = new ArrayList<>(oRoles.size());
        for (WnObj oRole : oRoles) {
            results.add(_to_wn_role(oRole));
        }
        // 计入缓存
        re = new WnRoleList(results);
        cache.put(uid, re);

        // 返回
        return re;
    }

    private WnRole _to_wn_role(WnObj oRole) {
        WnRoleType type = WnRoles.fromInt(oRole.getInt("role"));
        WnSimpleRole role = new WnSimpleRole();
        role.setType(type);
        role.setUserId(oRole.getString("uid"));
        role.setName(oRole.getString("grp"));
        return role;
    }

    @Override
    public WnRoleList queryRolesOf(String name) {
        // 准备查询条件
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("grp", name);
        q.sortBy("role", 1);

        // 执行查询
        List<WnObj> oRoles = io.query(q);
        List<WnRole> results = new ArrayList<>(oRoles.size());
        for (WnObj oRole : oRoles) {
            results.add(_to_wn_role(oRole));
        }

        // 返回结果
        return new WnRoleList(results);
    }

    @Override
    synchronized public WnRole addRole(String uid, String name, WnRoleType type) {
        // 首先清除缓存
        cache.remove(uid);

        // 准备查询条件
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("uid", uid);
        q.setv("grp", name);

        // 执行查询
        WnObj oRole = io.getOne(q);

        // 如果不存在，就创建一个
        if (null == oRole) {
            WnObj obj = new WnIoObj();
            obj.put("grp", name);
            obj.put("uid", uid);
            obj.put("role", type.getValue());
            oRole = io.create(oHome, obj);
        }
        // 如果已经存在，尝试更新
        else if (oRole.getInt("role", -100) == type.getValue()) {
            io.appendMeta(oRole, Wlang.map("role", type.getValue()));
        }

        // 返回结果
        return _to_wn_role(oRole);

    }

    @Override
    synchronized public void removeRole(WnRole role) {
        removeRole(role.getUserId(), role.getName());
    }

    @Override
    synchronized public void removeRole(String uid, String name) {
        // 删除缓存
        cache.remove(uid);

        // 从数据库里得到数据对象
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("uid", uid);
        q.setv("grp", name);
        WnObj oRole = io.getOne(q);

        // 删除数据库
        if (null != oRole) {
            io.delete(oRole);
        }

    }

    @Override
    synchronized public void clearCache() {
        cache.clear();
    }

}
