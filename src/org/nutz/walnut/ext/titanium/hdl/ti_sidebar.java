package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.sidebar.TiSidebarInput;
import org.nutz.walnut.ext.titanium.sidebar.TiSidebarOutput;
import org.nutz.walnut.ext.titanium.sidebar.TiSidebarService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_sidebar implements JvmHdl {

    private static TiSidebarService sidebars;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 初始化服务类
        if (null == sidebars) {
            synchronized (ti_sidebar.class) {
                if (null == sidebars) {
                    sidebars = Mvcs.getIoc().get(TiSidebarService.class);
                }
            }
        }
        // 寻找侧边栏文件
        WnObj oSidebar = null;

        // 路径集合
        String[] paths = hc.params.vals;
        if (paths.length == 0) {
            String ss = Strings.sBlank(sys.se.varString("SIDEBAR_PATH"),
                                       "/rs/ti/view/sidebar.json");
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
        final WnSecurityImpl secur = new WnSecurityImpl(sys.io, sys.usrService);
        final WnObj oSidebarHome = oSidebar.parent();

        // 准备解析
        TiSidebarInput input = sidebars.getInput(oSidebar);
        TiSidebarOutput output = sidebars.getOutput(input, sys.se, (roleName, path) -> {
            WnObj oTa = ".".equals(path) ? oSidebarHome : Wn.getObj(sys, path);
            // 根据权限码
            if (roleName.matches("^[rwx-]{3}$")) {
                int mode = Wn.Io.modeFromStr(roleName);
                return secur.test(oTa, mode);
            }
            // 根据角色
            String ta_grp = oTa.group();
            int shouldBeRole = Wn.ROLE.getRoleValue(roleName);
            // 管理员
            if (Wn.ROLE.ADMIN == shouldBeRole) {
                return sys.usrService.isAdminOfGroup(sys.me, ta_grp);
            }
            // 成员
            if (Wn.ROLE.MEMBER == shouldBeRole) {
                sys.usrService.isMemberOfGroup(sys.me, ta_grp);
            }
            // 权限写的不对，禁止
            return true;
        }, sys);

        // 输出
        sys.out.println(Json.toJson(output, hc.jfmt));
    }

}