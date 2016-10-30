package org.nutz.walnut.ext.app;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.bean.SidebarGroup;
import org.nutz.walnut.ext.app.bean.SidebarItem;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;

public class cmd_appSidebar extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(args, "cqnH", "^html$");

        // 确保会话有关键变量,自己的域名
        if (!sys.se.hasVar("SIDEBAR_DOMAIN"))
            sys.se.var("SIDEBAR_DOMAIN", sys.se.var("MY_GRP"));

        // 读取配置文件
        WnObj oConf = __find_conf(sys, params);
        SidebarGroup[] sgs = sys.io.readJson(oConf, SidebarGroup[].class);

        // 获取线程上下文
        WnContext wc = Wn.WC();

        // 展开所有动态加载项
        for (SidebarGroup sg : sgs) {
            List<SidebarItem> items = new LinkedList<SidebarItem>();
            for (SidebarItem si : sg.getItems()) {
                // 需要限制权限
                if (si.hasRoles()) {
                    if (!checkMatchRoleOrNot(sys, si.getRoles()))
                        continue;
                }
                // 动态: objs
                if (si.isType("objs")) {
                    String cmdText = si.getCmd();
                    String json = sys.exec2(cmdText);
                    List<WnBean> objs = Json.fromJsonAsList(WnBean.class, json);
                    for (WnObj o : objs) {
                        o.setTree(sys.io);

                        // 进一步检查侧边栏项目权限
                        if (__can_show(wc, o)) {
                            items.add(new SidebarItem(o, si));
                        }
                    }
                }
                // 动态: items
                else if (si.isType("items")) {
                    String cmdText = si.getCmd();
                    String json = sys.exec2(cmdText);
                    List<SidebarItem> items2 = Json.fromJsonAsList(SidebarItem.class, json);
                    for (SidebarItem item2 : items2) {
                        item2.setDefaultValue(si);
                    }
                    items.addAll(items2);
                }
                // 静态，看看是否有权限
                else {
                    String aph = Wn.normalizeFullPath(si.getPh(), sys);
                    WnObj o = sys.io.fetch(null, aph);
                    if (null != o && __can_show(wc, o)) {
                        // 更新路径
                        si.setPh(aph);
                        // 设置默认的 icon
                        if (!si.hasIcon()) {
                            si.setIcon(o.getString("icon"));
                        }
                        // 设置默认的文本
                        if (!si.hasText()) {
                            si.setText(o.name());
                        }

                        items.add(si);
                    }
                }
            }
            // 更新本组项目列表
            sg.setItems(items);
        }

        // 作为 HTML 输出
        if (params.is("html")) {
            for (SidebarGroup sg : sgs) {
                sys.out.println(sg.toHtml());
            }
        }
        // 输出 JSON 结果
        else {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            jfmt.setIgnoreNull(true);
            sys.out.println(Json.toJson(sgs, jfmt));
        }

    }

    public static boolean checkMatchRoleOrNot(WnSystem sys, String[] roles) {
        return sys.nosecurity(new Proton<Boolean>() {
            protected Boolean exec() {
                if (null != roles && roles.length > 0) {
                    for (String role : roles) {
                        String[] ss = Strings.splitIgnoreBlank(role, ":");
                        String roleName = ss[0];
                        String[] grps = Strings.splitIgnoreBlank(ss[1]);
                        for (String grp : grps) {
                            // 管理员
                            if ("ADMIN".equals(roleName)) {
                                if (sys.usrService.isAdminOfGroup(sys.me, grp))
                                    return true;
                            }
                            // 成员
                            else if ("MEMBER".equals(roleName)) {
                                if (sys.usrService.isMemberOfGroup(sys.me, grp))
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
     * @param o
     *            对象
     * @return 当前用户是否有权限在侧边栏上显示这个项目
     */
    private boolean __can_show(WnContext wc, WnObj o) {
        // 进一步检查侧边栏项目权限
        // 目录的话 必须是可进入的才可见
        if (o.isDIR() && null != wc.whenEnter(o, true)) {
            return true;
        }
        // 文件的话，必须是可读的才可见
        else if (o.isFILE() && null != wc.whenRead(o, true)) {
            return true;
        }
        // 通不过检查
        return false;
    }

    private WnObj __find_conf(WnSystem sys, ZParams params) {
        // 环境变量里最优先
        String phConf = sys.se.varString("SIDEBAR");

        // 否则看看是否指定了默认配置的位置
        if (Strings.isBlank(phConf))
            phConf = params.val(0);

        // 还是木有，那么采用系统的默认位置
        if (Strings.isBlank(phConf))
            phConf = "/etc/ui/sidebar.js";

        // 得到配置文件对象
        return Wn.checkObj(sys, phConf);
    }

}
