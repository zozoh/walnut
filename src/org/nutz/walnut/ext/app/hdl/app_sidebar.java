package org.nutz.walnut.ext.app.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.WnApps;
import org.nutz.walnut.ext.app.bean.SidebarGroup;
import org.nutz.walnut.ext.app.bean.SidebarItem;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;

@JvmHdlParamArgs(regex = "^html$", value = "cqnH")
public class app_sidebar implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 确保会话有关键变量,自己的域名
        NutMap vars = sys.session.getVars();
        if (!vars.has("SIDEBAR_DOMAIN"))
            vars.put("SIDEBAR_DOMAIN", sys.getMyGroup());

        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 读取配置文件
        WnObj oConf = __find_conf(sys, hc.params, oUIHomes);
        SidebarGroup[] sgs = sys.io.readJson(oConf, SidebarGroup[].class);

        // 获取线程上下文
        WnContext wc = Wn.WC();

        // 展开所有动态加载项
        for (SidebarGroup sg : sgs) {
            List<SidebarItem> items = new LinkedList<SidebarItem>();
            for (SidebarItem si : sg.getItems()) {
                // 需要限制权限
                if (si.hasRoles()) {
                    if (!WnApps.checkMatchRoleOrNot(sys, si.getRoles()))
                        continue;
                }
                // 动态: objs
                if (si.isType("objs")) {
                    String cmdText = si.getCmd();
                    String json = sys.exec2(cmdText);
                    List<WnBean> objs = Json.fromJsonAsList(WnBean.class, json);
                    for (WnObj o : objs) {
                        ((WnBean) o).setTree(sys.io);

                        // 进一步检查侧边栏项目权限
                        if (__can_show(wc, o)) {
                            items.add(new SidebarItem(si).updateBy(o));
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
                        items.add(si.updateBy(o));
                    }
                }
            }
            // 更新本组项目列表
            sg.setItems(items);
        }

        // 作为 HTML 输出
        if (hc.params.is("html")) {
            for (SidebarGroup sg : sgs) {
                sys.out.println(sg.toHtml());
            }
        }
        // 输出 JSON 结果
        else {
            sys.out.println(Json.toJson(sgs, hc.jfmt));
        }
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

    private WnObj __find_conf(WnSystem sys, ZParams params, List<WnObj> oUIHomes) {
        // 直接指定最优
        String phConf = params.val(0);

        // 环境变量次优
        if (Strings.isBlank(phConf)) {
            NutMap vars = sys.session.getVars();
            phConf = vars.getString("SIDEBAR");

            // 否则看看是否指定了默认配置的位置
            if (Strings.isBlank(phConf)) {
                phConf = params.getString("dft");

                // 还是木有，那么根据约定一次查找各个 UI 主目录
                if (Strings.isBlank(phConf)) {
                    for (WnObj oUIHome : oUIHomes) {
                        WnObj oSidebar = sys.io.fetch(oUIHome, "sidebar.js");
                        if (null != oSidebar)
                            return oSidebar;
                    }
                    // 没有找到 sidebar.js
                    throw Er.create("e.cmd.app.sidebar.noexists");
                }
            }
        }

        // 得到配置文件对象
        return Wn.checkObj(sys, phConf);
    }
}
