package com.site0.walnut.ext.data.app.hdl;

import java.util.HashSet;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.app.WnApps;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class app_mvto implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 归纳每个文件
        HashSet<String> memo = new HashSet<String>();
        String base = null;
        HashSet<String> filter = new HashSet<String>();

        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 循环
        for (int i = 0; i < hc.params.vals.length; i++) {
            // 得到类型
            String type = WnApps.getType(sys, hc.params.vals[i]);

            // 不要重复搞类型了
            if (memo.contains(type))
                continue;

            // 记录以防止重复
            memo.add(type);

            // 得到类型的主目录
            WnObj oFType = WnApps.checkFTypeObj(sys, type, oUIHomes);

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
        NutMap reMap = Wlang.map("base", base).setv("filter", filter);
        sys.out.println(Json.toJson(reMap, hc.jfmt));
    }

}
