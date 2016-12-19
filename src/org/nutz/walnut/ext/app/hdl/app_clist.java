package org.nutz.walnut.ext.app.hdl;

import java.util.ArrayList;
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
public class app_clist implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        String str = hc.params.val_check(0);

        // 得到类型
        String type = WnApps.getType(sys, str);

        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 得到类型的主目录
        WnObj oFType = WnApps.checkFTypeObj(sys, type, oUIHomes);

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
                oFType = WnApps.checkFTypeObj(sys, ftype, oUIHomes);
                map = sys.io.readJson(oFType, NutMap.class);
                map.put("tp", ftype);
                map.remove("actions");
                map.remove("editors");
                list.add(map);
            }

            sys.out.println(Json.toJson(list, hc.jfmt));
        }
    }

}
