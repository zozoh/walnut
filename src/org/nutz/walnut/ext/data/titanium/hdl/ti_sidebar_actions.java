package org.nutz.walnut.ext.data.titanium.hdl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.each.WnEachIteratee;

public class ti_sidebar_actions implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        //
        // 读取侧边栏信息
        //
        List<NutMap> sidebar;
        String json = sys.in.readAll();
        if (Ws.isBlank(json)) {
            json = sys.exec2("ti sidebar -qn");
        }
        Object jo = Wlang.anyToObj(json);
        if (jo instanceof Map) {
            NutMap jom = Wlang.anyToMap(jo);
            sidebar = jom.getList("sidebar", NutMap.class);
        }
        // 作为集合
        else {
            sidebar = new LinkedList<>();
            Wlang.each(jo, new WnEachIteratee<Object>() {
                public void invoke(int index, Object ele, Object src) {
                    NutMap sideItem = Wlang.anyToMap(ele);
                    sidebar.add(sideItem);
                }
            });
        }

        // 防守一波
        if (null == sidebar) {
            sys.out.println("[]");
        }

        // 循环输出字段信息
        List<NutMap> actions = new LinkedList<>();
        for (NutMap sideItem : sidebar) {
            this.joinSidebarItem(actions, sideItem);
        }

        // 输出结果
        JsonFormat jfmt = Cmds.gen_json_format(hc.params);
        String str = Json.toJson(actions, jfmt);
        sys.out.println(str);
    }

    public void joinSidebarItem(List<NutMap> actions, NutMap sideItem) {
        // 防守
        if (null == sideItem) {
            return;
        }

        // 分析自己
        NutMap a = new NutMap();
        if (sideItem.has("icon")) {
            a.put("icon", sideItem.get("icon"));
        }
        if (sideItem.has("title")) {
            a.put("text", sideItem.get("title"));
        }
        if (sideItem.has("path")) {
            String path = sideItem.getString("path");
            String name = Wpath.getMajorName(path);
            a.put("value", "can_" + Ws.snakeCase(name));
        }
        if (!a.isEmpty()) {
            actions.add(a);
        }

        // 递归
        List<NutMap> items = sideItem.getAsList("items", NutMap.class);
        if (null != items) {
            for (NutMap it : items) {
                joinSidebarItem(actions, it);
            }
        }

    }

}
