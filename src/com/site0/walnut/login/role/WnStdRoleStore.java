package com.site0.walnut.login.role;

import java.util.ArrayList;
import java.util.List;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnIoObj;
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
    public WnRoleList queryRolesOf(String grp) {
        // 准备查询条件
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("grp", grp);
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

    protected WnRole _add_role(String uid, String grp, WnRoleType type, String unm) {
        WnObj obj = new WnIoObj();
        obj.put("grp", grp);
        obj.put("uid", uid);
        obj.put("unm", unm);
        obj.put("type", type.toString());
        obj.put("role", type.getValue());

        WnObj oRole = io.create(oHome, obj);

        // 返回结果
        return _to_wn_role(oRole);
    }

    protected WnRole _set_role(String uid, String grp, WnRoleType type, String unm) {
        // 准备查询条件
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("uid", uid);
        q.setv("grp", grp);

        // 执行查询
        WnObj oRole = io.getOne(q);
        if (oRole.getInt("role") != type.getValue() || !oRole.is("unm", unm)) {
            NutMap delta = new NutMap();
            delta.put("unm", unm);
            delta.put("type", type.toString());
            delta.put("role", type.getValue());
            io.appendMeta(oRole, delta);
        }

        // 返回结果
        return _to_wn_role(oRole);
    }

    protected void _remove_role(String uid, String grp) {
        // 从数据库里得到数据对象
        WnQuery q = Wn.Q.pid(oHome);
        q.setv("uid", uid);
        q.setv("grp", grp);
        WnObj oRole = io.getOne(q);

        // 删除数据库
        if (null != oRole) {
            io.delete(oRole);
        }
    }

}
