package org.nutz.walnut.ext.app;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;

public class cmd_appmenu extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        // 获取对象
        WnObj o;
        if (params.vals.length > 0) {
            o = Wn.checkObj(sys, params.vals[0]);
        }
        // 默认获取当前路径
        else {
            o = this.getCurrentObj(sys);
        }

        // 根据对象类型获取菜单
        WnObj oMenuHome = sys.io.fetch(null, Wn.normalizePath("~/.ui/menu", sys));
        WnObj oMenu = null;
        // 文件夹
        if (o.isDIR()) {
            oMenu = sys.io.fetch(oMenuHome, Strings.sBlank(o.type(), "folder") + ".js");
        }
        // 文件
        else {
            // 有用 Type
            if (o.hasType()) {
                oMenu = sys.io.fetch(oMenuHome, o.type() + ".js");
            }
            // 找不到就用 MIME
            if (null == oMenu) {
                oMenu = sys.io.fetch(oMenuHome, "mime_" + o.mime().replace('/', '_') + ".js");
            }
        }
        // 没找到，那么就一定查找
        if (null == oMenu) {
            oMenu = sys.io.fetch(oMenuHome, "_unknown.js");
        }

        // 还是没有？ 那么就返回空吧
        if (null == oMenu) {
            sys.out.println("{nomenu:true, actions:[]}");
            return;
        }

        // 分析动作文件，根据动作内容和权限，过滤菜单项
        NutMap map = sys.io.readJson(oMenu, NutMap.class);
        List<String> actions = map.getList("actions", String.class);
        List<String> list = new ArrayList<String>(actions.size());
        boolean lastIsGroup = false;

        // "@:r:new", # 永远显示:需要读权限:new操作
        // ":w:delete", # 在菜单里显示:需要写权限:new操作
        // "~", # 分隔符
        // "::properties" # 在菜单里显示:不校验权限:属性操作
        WnContext wc = Wn.WC();
        for (String astr : actions) {
            String str = Strings.trim(astr);
            String[] ss = str.split(":");
            // 如果当前是个组，连续多个组会保持一份
            if ("~".equals(str)) {
                if (lastIsGroup)
                    continue;
                lastIsGroup = true;
                list.add(str);
                continue;
            }
            // 需要验证权限
            if (ss[1].length() > 0) {
                int mod = 0;
                if (ss[1].indexOf('r') >= 0)
                    mod |= Wn.Io.R;
                if (ss[1].indexOf('w') >= 0)
                    mod |= Wn.Io.W;
                if (ss[1].indexOf('x') >= 0)
                    mod |= Wn.Io.X;
                if (mod > 0) {
                    if (!wc.testSecurity(o, mod))
                        continue;
                }
            }
            // 保存到结果列表
            list.add(str);

            // 那么标记一下组
            lastIsGroup = false;
        }

        // 更新到新的动作链表
        map.put("actions", list);
        sys.out.println(Json.toJson(map));
    }

}
