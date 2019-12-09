package org.nutz.walnut.impl.auth;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnGroupAccount;
import org.nutz.walnut.api.auth.WnGroupRoleService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.api.auth.WnGroupRole;

public class WnGroupRoleServiceImpl implements WnGroupRoleService {

    WnIo io;

    private WnObj __dir;

    public WnGroupRoleServiceImpl(WnIo io) {
        this.io = io;
    }

    @Override
    public WnObj getSysRoleDir() {
        if (null == __dir) {
            synchronized (WnGroupRoleServiceImpl.class) {
                if (null == __dir) {
                    String aph = "/sys/role/";
                    __dir = io.createIfNoExists(null, aph, WnRace.DIR);
                }
            }
        }
        return __dir;
    }

    @Override
    public WnGroupRole getGroupRole(WnAccount user, String groupName) {
        WnObj oSysRoleDir = this.getSysRoleDir();
        // 默认组权限： 0 - GUEST
        int role = 0;
        // 尝试获取
        WnObj oR = getRoleObj(oSysRoleDir, user, groupName);

        // 为了兼容老代码，如果试图用名字获取
        if (null == oR) {
            String aph = Wn.appendPath("/sys/grp", groupName, "people", user.getId());
            oR = io.fetch(null, aph);
            // 如果存在，则复制到新规则里
            if (null != oR) {
                role = oR.getInt("role", 0);
                WnObj newR = io.create(oSysRoleDir, "${id}", WnRace.FILE);
                NutMap meta = new NutMap();
                meta.put("uid", user.getId());
                meta.put("grp", groupName);
                meta.put("role", role);
                io.appendMeta(newR, meta);
            }
        }
        // 获取角色值
        else {
            role = oR.getInt("role", 0);
        }

        // 默认是GUEST
        return WnGroupRole.parseInt(role);
    }

    private WnObj getRoleObj(WnObj oSysRoleDir, WnAccount user, String groupName) {
        WnQuery q = Wn.Q.pid(oSysRoleDir);
        q.setv("uid", user.getId());
        q.setv("grp", groupName);
        WnObj oR = io.getOne(q);
        return oR;
    }

    @Override
    public void setGroupRole(WnAccount user, String groupName, WnGroupRole role) {
        // null 表示要移除
        if (null == role) {
            this.removeGroupRole(user, groupName);
            return;
        }

        WnObj oSysRoleDir = this.getSysRoleDir();
        WnObj oR = getRoleObj(oSysRoleDir, user, groupName);

        // 如果有的话，则改值
        if (null != oR) {
            int r = oR.getInt("role", 0);
            WnGroupRole gr = WnGroupRole.parseInt(r);
            if (gr != role) {
                io.appendMeta(oR, Lang.map("role", role.getValue()));
            }
        }
        // 如果获取失败，则新创建一个
        else {
            oR = io.create(oSysRoleDir, "${id}", WnRace.FILE);
            NutMap meta = new NutMap();
            meta.put("uid", user.getId());
            meta.put("grp", groupName);
            meta.put("role", role.getValue());
            io.appendMeta(oR, meta);
        }
    }

    @Override
    public WnGroupRole removeGroupRole(WnAccount user, String groupName) {
        WnObj oSysRoleDir = this.getSysRoleDir();
        WnObj oR = getRoleObj(oSysRoleDir, user, groupName);
        if (null != oR) {
            int r = oR.getInt("role", 0);
            io.delete(oR);
            return WnGroupRole.parseInt(r);
        }
        return null;
    }

    @Override
    public List<WnGroupAccount> getAccounts(String groupName) {
        WnObj oSysRoleDir = this.getSysRoleDir();
        WnQuery q = Wn.Q.pid(oSysRoleDir);
        q.setv("grp", groupName);
        List<WnGroupAccount> list = new LinkedList<>();
        io.each(q, (index, oR, len) -> {
            // 得到权限值
            int r = oR.getInt("role", 0);
            WnGroupRole role = WnGroupRole.parseInt(r);
            // 得到账户
            String uid = oR.getString("uid");
            WnObj oU = io.get(uid);
            WnAccount u;
            // 看来用户被删了
            if (null == oU) {
                u = new WnAccount();
                u.setId(uid);
                u.setName("!nil!");
            }
            // 生成账户对象
            else {
                u = new WnAccount(oU);
            }

            // 计入返回结果
            WnGroupAccount ga = new WnGroupAccount();
            ga.setGroupName(groupName);
            ga.setAccount(u);
            ga.setRole(role);

            list.add(ga);
        });

        return list;
    }

    @Override
    public List<WnGroupAccount> getGroups(WnAccount user) {
        WnObj oSysRoleDir = this.getSysRoleDir();
        WnQuery q = Wn.Q.pid(oSysRoleDir);
        q.setv("uid", user.getId());
        List<WnGroupAccount> list = new LinkedList<>();
        io.each(q, (index, oR, len) -> {
            // 得到权限值
            int r = oR.getInt("role", 0);
            WnGroupRole role = WnGroupRole.parseInt(r);
            // 得到组名
            String groupName = oR.getString("grp");

            // 计入返回结果
            WnGroupAccount ga = new WnGroupAccount();
            ga.setGroupName(groupName);
            ga.setAccount(user);
            ga.setRole(role);

            list.add(ga);
        });

        return list;
    }

    @Override
    public boolean isRoleOfGroup(WnGroupRole role, WnAccount user, String... groupNames) {
        for (String groupName : groupNames) {
            WnGroupRole r = this.getGroupRole(user, groupName);
            if (role == r) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAdminOfGroup(WnAccount user, String... groupNames) {
        return this.isRoleOfGroup(WnGroupRole.ADMIN, user, groupNames);
    }

    @Override
    public boolean isMemberOfGroup(WnAccount user, String... groupNames) {
        for (String groupName : groupNames) {
            WnGroupRole r = this.getGroupRole(user, groupName);
            if (WnGroupRole.ADMIN == r || WnGroupRole.MEMBER == r) {
                return true;
            }
        }
        return false;
    }

}
