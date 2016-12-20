package org.nutz.walnut.ext.app.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.app.WnApps;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class app_editor implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 得到编辑器名称
        String editorName = hc.params.val_check(0);

        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 读取编辑器信息
        NutMap map = WnApps.loadEditorInfo(sys, editorName, oUIHomes);

        // 输出
        sys.out.println(Json.toJson(map, hc.jfmt));
    }

}
