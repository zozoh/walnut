package com.site0.walnut.ext.data.app;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.util.Wn;

public abstract class WnApps {

    /**
     * 读取编辑器信息
     * 
     * @param sys
     *            系统上下文
     * @param editorName
     *            编辑器名称
     * @param oUIHomes
     *            UI 主目录列表
     * @return 编辑器配置信息
     */
    public static NutMap loadEditorInfo(WnSystem sys, String editorName, List<WnObj> oUIHomes) {
        // 编辑器对象
        WnObj oEditor = null;

        // 得到编辑器配置信息
        String rph = "editors/" + editorName + ".js";
        for (WnObj oUIHome : oUIHomes) {
            oEditor = sys.io.fetch(oUIHome, rph);
            if (null != oEditor)
                break;
        }

        // 必须有编辑器
        if (null == oEditor)
            throw Er.create("e.cmd.app.editor.noexists", editorName);

        // 读取内容
        NutMap map = sys.io.readJson(oEditor, NutMap.class);
        return map;
    }

    /**
     * @param sys
     *            系统上下文
     * @param roles
     *            角色列表
     * @return 当前用户是否匹配用户列表
     */
    public static boolean checkMatchRoleOrNot(WnSystem sys, String[] roleNames) {
        return sys.nosecurity(new Proton<Boolean>() {
            protected Boolean exec() {
                if (null != roleNames && roleNames.length > 0) {
                    WnUser me = sys.getMe();
                    WnRoleList roles = sys.auth.getRoles(me);
                    for (String role : roleNames) {
                        String[] ss = Strings.splitIgnoreBlank(role, ":");
                        String roleName = ss[0];
                        String[] grps = Strings.splitIgnoreBlank(ss[1]);
                        for (String grp : grps) {
                            // 管理员
                            if ("ADMIN".equals(roleName)) {
                                if (roles.isAdminOfRole(grp))
                                    return true;
                            }
                            // 成员
                            else if ("MEMBER".equals(roleName)) {
                                if (roles.isMemberOfRole(grp))
                                    return true;
                            }
                            // 角色名称错误
                            else {
                                return false;
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * @param sys
     *            系统上下文
     * @return UI 的主目录列表
     */
    public static List<WnObj> getUIHomes(WnSystem sys) {
        return Wn.getPathObjList(sys, "UI_PATH", "/etc/ui");
    }

    /**
     * 从UI的主目录里选取一个文件
     * 
     * @param sys
     *            系统上下文
     * @param actionName
     *            动作名
     * @param oUIHomes
     *            UI 的主目录列表
     * @return 对象
     */
    public static WnObj checkActionObj(WnSystem sys, String actionName, List<WnObj> oUIHomes) {
        String actionPath = "actions/" + actionName + ".js";
        WnObj o = Wn.getObjIn(sys, actionPath, oUIHomes);
        if (null == o)
            throw Er.create("e.cmd.app.action.noexists", actionName);
        return o;
    }

    /**
     * @param sys
     *            系统上下文
     * @param type
     *            类型名
     * @param oUIHomes
     *            UI 的主目录列表
     * @return 对应文件类型的配置信息
     * 
     * @throws "e.cmd.app.ftype.noexists"
     *             没找到配置信息
     */
    public static WnObj checkFTypeObj(WnSystem sys, String type, List<WnObj> oUIHomes) {
        WnObj o = Wn.getObjIn(sys, "ftypes/" + type + ".js", oUIHomes);
        if (null == o)
            o = Wn.getObjIn(sys, "ftypes/_unknown.js", oUIHomes);
        if (null == o)
            throw Er.create("e.cmd.app.ftype.noexists", type);
        return o;
    }

    /**
     * @param sys
     *            系统上下文
     * @param str
     *            参数字符串，可能是一个对象路径或者 "type:xxx" 来声明的类型
     * @return 一个文件类型
     */
    public static String getType(WnSystem sys, String str) {
        // 直接就是类型
        if (str.startsWith("type:")) {
            return str.substring("type:".length());
        }
        // 根据文件获取类型
        WnObj o = Wn.checkObj(sys, str);

        String type = o.type();
        if (Strings.isBlank(type)) {
            type = o.isDIR() ? "folder" : "_unknown";
        }

        return type;
    }

    // =================================================================
    // 不许实例化
    private WnApps() {}

}
