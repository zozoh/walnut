package org.nutz.walnut.ext.data.titanium.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.titanium.api.TiGenMapping;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs("cqn")
public class ti_gen_mapping implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String as = hc.params.getString("as", "form");
        String fph = hc.params.getString("f");
        String type = hc.params.val(0, "export");
              

        // 读取字段
        String json;
        // 从标准输入
        if (Ws.isBlank(fph)) {
            json = sys.in.readAll();
        }
        // 从文件
        else {
            WnObj o = Wn.checkObj(sys, fph);
            json = sys.io.readText(o);
        }
        List<NutMap> fields = Json.fromJsonAsList(NutMap.class, json);
        
     // 解析字典
        String dictsJson = null;
        String dicts = hc.params.get("dicts");
        if("true".equals(dicts)) {
            dictsJson = sys.in.readAll();
        }
        // 读取路径
        else if(!Ws.isBlank(dicts)){
            WnObj oD = Wn.checkObj(sys, dicts);
            dictsJson = sys.io.readText(oD);
        }
        

        // 准备处理器
        String[] whiteList = hc.params.getAs("white", String[].class);
        String[] blackList = hc.params.getAs("black", String[].class);
        String key = String.format("%s_%s", type, as);
        TiGenMapping gm = TiGenMapping.getInstance(key);
        gm.setWhiteList(whiteList);
        gm.setBlackList(blackList);
        gm.setDicts(dictsJson);

        // 输出
        NutMap mapping = gm.genMapping(fields);

        JsonFormat jfmt = Cmds.gen_json_format(hc.params);
        String str = Json.toJson(mapping, jfmt);
        sys.out.println(str);
    }

}
