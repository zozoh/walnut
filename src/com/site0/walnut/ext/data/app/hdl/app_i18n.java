package com.site0.walnut.ext.data.app.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.app.WnApps;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(regex = "^(merge)$", value = "cqn")
public class app_i18n implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到语言
        String lang = "zh-cn";
        if (hc.params.vals.length > 0)
            lang = hc.params.val(0);

        // 是否合并
        boolean isMerge = hc.params.is("merge");

        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 准备消息字符串集合
        NutMap msg = new NutMap();

        // 循环
        String rph = "i18n/" + lang + ".js";
        for (WnObj oUIHome : oUIHomes) {
            WnObj o = sys.io.fetch(oUIHome, rph);
            // 存在这个文件
            if (null != o) {
                NutMap map = sys.io.readJson(o, NutMap.class);
                msg.mergeWith(map, true);
                // 不合并的话就退出了
                if (!isMerge)
                    break;
            }
        }

        // 输出
        sys.out.println(Json.toJson(msg, hc.jfmt));
    }

}
