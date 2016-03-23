package org.nutz.walnut.ext.app;

import java.util.HashSet;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_appMoveTo extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "cqn");

        // 归纳每个文件
        HashSet<String> memo = new HashSet<String>();
        String base = null;
        HashSet<String> filter = new HashSet<String>();
        for (int i = 0; i < params.vals.length; i++) {
            String str = params.vals[i];

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
                    type = o.isDIR() ? "folder" : "_unknown";
                }
            }

            // 不要重复搞类型了
            if (memo.contains(type))
                continue;

            memo.add(type);

            // 得到类型的主目录
            WnObj oFTypeHome = Wn.checkObj(sys, "~/.ui/ftypes");
            WnObj oFType = sys.io.fetch(oFTypeHome, type + ".js");

            // 如果木有，则用 _unknowns
            if (null == oFType) {
                oFType = sys.io.fetch(oFTypeHome, "_unknown.js");
            }

            // 读取内容
            NutMap map = sys.io.readJson(oFType, NutMap.class);
            NutMap moveTo = map.getAs("moveTo", NutMap.class);

            // 开始归纳 Base
            if (null != moveTo) {
                if (null == base) {
                    base = moveTo.getString("base", "~");
                }
                // 找交集吧
                else if (!"~".equals(base)) {
                    base = Disks.getIntersectPath(base, moveTo.getString("base", "~"), "~");
                }

                // 合并 filter
                String flt = moveTo.getString("filter");
                if (!Strings.isBlank(flt))
                    filter.add(flt);
            }
        }

        // 输出对象
        NutMap reMap = Lang.map("base", base).setv("filter", filter);
        JsonFormat jfmt = this.gen_json_format(params);
        sys.out.println(Json.toJson(reMap, jfmt));
    }

}
