package com.site0.walnut.ext.data.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnGroupRole;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.titanium.sidebar.TiSidebarCheckItemByRoleName;
import com.site0.walnut.ext.data.titanium.sidebar.TiSidebarCheckPvg;
import com.site0.walnut.ext.data.titanium.sidebar.TiSidebarInput;
import com.site0.walnut.ext.data.titanium.sidebar.TiSidebarOutput;
import com.site0.walnut.ext.data.titanium.sidebar.TiSidebarService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wsum;
import com.site0.walnut.util.validate.impl.AutoMatch;

@JvmHdlParamArgs("cqn")
public class ti_sidebar implements JvmHdl {

    private static TiSidebarService sidebars;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 初始化服务类
        if (null == sidebars) {
            synchronized (ti_sidebar.class) {
                if (null == sidebars) {
                    sidebars = hc.ioc.get(TiSidebarService.class);
                }
            }
        }
        // 寻找侧边栏文件
        WnObj oSidebar = null;

        // 路径集合
        String[] paths = hc.params.vals;
        if (paths.length == 0) {
            String ss = sys.session.getVars().getString("SIDEBAR_PATH", "/rs/ti/view/sidebar.json");
            paths = Strings.splitIgnoreBlank(ss, ":");
        }
        // 查找
        if (paths.length > 0) {
            for (String ph : paths) {
                oSidebar = Wn.getObj(sys, ph);
                if (null != oSidebar)
                    break;
            }
        }

        // 木有找到
        if (null == oSidebar) {
            throw Er.create("e.cmd.ti_sidebar.noexists", paths);
        }

        // 准备权限检查接口
        final WnSecurityImpl secur = new WnSecurityImpl(sys.io, sys.auth);
        final WnObj oSidebarHome = oSidebar.parent();
        WnAccount me = sys.getMe();

        // 判断一下当前用户权限
        String myGroupName = sys.session.getMyGroup();
        boolean IamAdmin = sys.auth.isAdminOfGroup(me, myGroupName);
        boolean IamMember = IamAdmin ? true : sys.auth.isMemberOfGroup(me, myGroupName);

        // 准备解析
        TiSidebarInput input = sidebars.getInput(oSidebar);

        // 检查系统权限
        TiSidebarCheckItemByRoleName checkRole = (roleName, path) -> {
            String ta_grp;
            // @XXX 直接表示组名
            if (path.startsWith("@")) {
                ta_grp = path.substring(1).trim();
            }
            // 根据路径
            else {
                WnObj oTa = ".".equals(path) ? oSidebarHome : Wn.getObj(sys, path);
                // 根据权限码
                if (roleName.matches("^[rwx-]{3}$")) {
                    int mode = Wn.Io.modeFromStr(roleName);
                    return secur.test(oTa, mode);
                }
                // 根据角色
                ta_grp = oTa.group();
            }
            WnGroupRole shouldBeRole = WnGroupRole.valueOf(roleName);
            // 管理员
            if (WnGroupRole.ADMIN == shouldBeRole) {
                if (ta_grp.equals(myGroupName))
                    return IamAdmin;
                return sys.auth.isAdminOfGroup(me, ta_grp);
            }
            // 成员
            if (WnGroupRole.MEMBER == shouldBeRole) {
                if (ta_grp.equals(myGroupName))
                    return IamMember;
                return sys.auth.isMemberOfGroup(me, ta_grp);
            }
            // 权限写的不对，禁止
            return false;
        };

        // 域账户登陆进来的用户，可能需要检测一下自定义权限，所以，先加载一下
        NutBean myAvaPvg = sys.getAllMyPvg();

        // 检查自定义权限
        TiSidebarCheckPvg checkPvg = (pvg) -> {
            // 域管理员，一定通过检查
            if (IamAdmin)
                return true;
            return new AutoMatch(pvg).match(myAvaPvg);
        };

        // 渲染输出
        TiSidebarOutput output = sidebars.getOutput(input, sys.session, checkRole, checkPvg, sys);

        // 设置状态存储键
        output.setStatusStoreKey(Wsum.md5AsString(oSidebar.id()));

        // 输出
        sys.out.println(Json.toJson(output, hc.jfmt));
    }

}
