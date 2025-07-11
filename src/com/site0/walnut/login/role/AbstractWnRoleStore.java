package com.site0.walnut.login.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.usr.WnUser;

public abstract class AbstractWnRoleStore implements WnRoleStore {

    protected WnIo io;
    protected NutBean sessionVars;
    // TODO 这是个简单缓存，以后在扩展更优化的缓存策略
    protected Map<String, WnRoleList> cache;

    public AbstractWnRoleStore(WnIo io, NutBean sessionVars) {
        this.io = io;
        this.sessionVars = sessionVars;
        this.cache = new HashMap<>();
    }

    @Override
    synchronized public WnRoleList getRoles(WnUser u) {
        return getRoles(u.getId());
    }

    protected WnRole _to_wn_role(NutBean oRole) {
        WnRole role = new WnSimpleRole();
        role.fromBean(oRole);
        return role;
    }

    protected abstract List<WnRole> _get_roles(String uid);

    @Override
    synchronized public WnRoleList getRoles(String uid) {
        // 尝试缓存
        WnRoleList re = cache.get(uid);
        if (null != re) {
            return re;
        }

        List<WnRole> results = _get_roles(uid);
        // 计入缓存
        re = new WnRoleList(results);
        cache.put(uid, re);

        // 返回
        return re;
    }

    protected abstract WnRole _add_role(String uid, String name, WnRoleType type);

    @Override
    public WnRole addRole(String uid, String name, WnRoleType type) {
        // 执行真正的添加操作
        WnRole re = _add_role(uid, name, type);

        // 最后清除缓存
        synchronized (this) {
            cache.remove(uid);
        }

        return re;

    }

    protected abstract void _remove_role(String uid, String name);

    @Override
    synchronized public void removeRole(String uid, String name) {
        // 执行真正的删除操作
        _remove_role(uid, name);

        // 最后清除缓存
        synchronized (this) {
            cache.remove(uid);
        }

    }

    @Override
    public synchronized void removeRole(WnRole role) {
        removeRole(role.getUserId(), role.getName());
    }

    @Override
    synchronized public void clearCache() {
        cache.clear();
    }

}