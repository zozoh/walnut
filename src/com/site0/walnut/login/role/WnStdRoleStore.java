package com.site0.walnut.login.role;

import java.util.ArrayList;
import java.util.List;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class WnStdRoleStore extends AbstractWnRoleStore {

    private WnObj oHome;

    public WnStdRoleStore(WnIo io, NutBean sessionVars, String homePath) {
        super(io, sessionVars);
        this.oHome = io.createIfNoExists(null, homePath, WnRace.DIR);

    }

    protected List<WnRole> _get_roles(String uid) {
        // 真正查询
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("uid", uid);
        q.sortBy("role", 1);
        List<WnObj> oRoles = io.query(q);
        List<WnRole> results = new ArrayList<>(oRoles.size());
        for (WnObj oRole : oRoles) {
            results.add(_to_wn_role(oRole));
        }
        return results;
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

    protected WnRole _add_role(String uid, String name, WnRoleType type) {
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

    protected void _remove_role(String uid, String name) {
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

}
