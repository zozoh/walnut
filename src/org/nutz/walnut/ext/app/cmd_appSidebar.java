package org.nutz.walnut.ext.app;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.bean.SidebarGroup;
import org.nutz.walnut.ext.app.bean.SidebarItem;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_appSidebar extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(args, "cqnH", "^html$");

        // 读取配置文件
        WnObj oConf = __find_conf(sys, params);
        SidebarGroup[] sgs = sys.io.readJson(oConf, SidebarGroup[].class);

        // 展开所有动态加载项
        for (SidebarGroup sg : sgs) {
            List<SidebarItem> items = new LinkedList<SidebarItem>();
            for (SidebarItem si : sg.getItems()) {
                // 动态: objs
                if (si.isType("objs")) {
                    String json = sys.exec2(si.getCmd());
                    List<WnBean> objs = Json.fromJsonAsList(WnBean.class, json);
                    for (WnObj o : objs) {
                        o.setTree(sys.io);
                        items.add(new SidebarItem(o));
                    }
                }
                // 动态: items
                else if (si.isType("items")) {
                    String json = sys.exec2(si.getCmd());
                    List<SidebarItem> items2 = Json.fromJsonAsList(SidebarItem.class, json);
                    items.addAll(items2);
                }
                // 静态，看看是否有权限
                else {
                    WnObj o = Wn.getObj(sys, si.getPh());
                    if (null != o)
                        items.add(si);
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

    private WnObj __find_conf(WnSystem sys, ZParams params) {
        // 首先找一下配置文件
        WnObj oConf;

        // 看看是否指定了配置文件位置
        String phConf = params.val(0);

        // 然后看看是否环境变量里指定了
        if (Strings.isBlank(phConf))
            phConf = sys.se.varString("SIDEBAR");

        // 得到配置文件对象
        if (!Strings.isBlank(phConf)) {
            oConf = Wn.checkObj(sys, phConf);
        }
        // 按照默认规则搜索一下
        else {
            oConf = Wn.getObj(sys, "~/.ui/sidebar.js");
            if (null == oConf)
                oConf = sys.io.fetch(null, "/etc/ui/sidebar.js");
            if (null == oConf)
                throw Er.create("e.cmd.appSidebar.noconf");
        }
        return oConf;
    }

}
