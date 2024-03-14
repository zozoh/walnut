package com.site0.walnut.ext.data.app.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.app.WnApps;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class app_actions implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 准备返回值
        List<String> list = new ArrayList<>(hc.params.vals.length);

        // 循环读取命令文件
        for (String actionName : hc.params.vals) {
            WnObj oAction = WnApps.checkActionObj(sys, actionName, oUIHomes);
            String text = sys.io.readText(oAction);
            list.add(text);
        }

        // 最后输出
        sys.out.println(Json.toJson(list, hc.jfmt));
    }

}
