package org.nutz.walnut.ext.app;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_appclist extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "cqn");
        String str = params.vals[0];

        // 得到类型
        String type;

        // 直接就是类型
        if (str.startsWith("type:")) {
            type = str.substring("type:".length());
        }
        // 根据文件获取类型
        else {
            WnObj o = Wn.checkObj(sys, str);

            type = o.type();
            if (Strings.isBlank(type)) {
                type = o.isDIR() ? "folder" : "txt";
            }
        }

        // 得到类型的主目录
        WnObj oFTypeHome = Wn.checkObj(sys, "~/.ui/ftypes");
        WnObj oFType = sys.io.check(oFTypeHome, type + ".js");

        // 读取内容
        NutMap map = sys.io.readJson(oFType, NutMap.class);

        // 那么开始找吧
        String[] clist = map.getArray("create", String.class);

        // 麻都木有，输出个空数组
        if (null == clist || clist.length == 0) {
            sys.out.println("[]");
        }
        // 开始挨个找
        else {
            List<NutMap> list = new ArrayList<NutMap>(clist.length);
            for (String ftype : clist) {
                oFType = sys.io.check(oFTypeHome, ftype + ".js");
                map = sys.io.readJson(oFType, NutMap.class);
                map.put("tp", ftype);
                map.remove("actions");
                map.remove("editors");
                list.add(map);
            }
            JsonFormat jfmt = this.gen_json_format(params);
            sys.out.println(Json.toJson(list, jfmt));
        }

    }

}
