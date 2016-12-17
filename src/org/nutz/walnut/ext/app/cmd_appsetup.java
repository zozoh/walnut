package org.nutz.walnut.ext.app;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.ZParams;

public class cmd_appsetup extends JvmExecutor {

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
            o = sys.getCurrentObj();
        }

        // 根据对象类型获取菜单
        WnObj oUiHome = sys.io.fetch(null, Wn.normalizePath("~/.ui", sys));
        WnObj oFType = null;
        if (null != oUiHome) {
            // 指定的类型
            if (params.has("tp")) {
                String tp = params.get("tp");
                oFType = sys.io.fetch(oUiHome, "ftypes/" + tp + ".js");
            }
            // 根据对象查找
            else {
                oFType = __find_ftype(sys, o, oUiHome);
            }
        }
        // 还是没有？ 那么就返回空吧
        if (null == oFType) {
            sys.out.println("{actions:[]}");
            return;
        }

        // 要输出的对象，进行整理
        NutMap map = sys.io.readJson(oFType, NutMap.class);

        // 分析动作文件，根据动作内容和权限，过滤菜单项
        List<String> list = __filter_actions(o, map.getList("actions", String.class));
        map.put("actions", list);

        // 输出
        sys.out.println(Json.toJson(map));
    }

    private List<String> __filter_actions(WnObj o, List<String> actions) {
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
        return list;
    }

    private WnObj __find_ftype(WnSystem sys, WnObj o, WnObj oUiHome) {
        WnObj oFType = null;
        // 文件夹
        if (o.isDIR()) {
            oFType = sys.io.fetch(oUiHome, "ftypes/" + Strings.sBlank(o.type(), "folder") + ".js");
        }
        // 文件
        else {
            // 有用 Type
            if (o.hasType()) {
                oFType = sys.io.fetch(oUiHome, "ftypes/" + o.type() + ".js");
            }
            // 找不到就用 MIME
            if (null == oFType) {
                WnObj oMimeHome = sys.io.fetch(oUiHome, "mimes");
                if (null != oMimeHome) {
                    String[] ss = Strings.splitIgnoreBlank(o.mime(), "/");
                    oFType = sys.io.fetch(oMimeHome, Lang.concat("_", ss) + ".js");
                    // 还找不到，就用 mime 的分类名
                    if (null == oFType) {
                        oFType = sys.io.fetch(oMimeHome, ss[0] + ".js");
                    }
                }
            }
        }
        // 没找到，那么就一定查找
        if (null == oFType) {
            oFType = sys.io.fetch(oUiHome, o.isDIR() ? "ftypes/folder.js" : "ftypes/_unknown.js");
        }
        return oFType;
    }
}
