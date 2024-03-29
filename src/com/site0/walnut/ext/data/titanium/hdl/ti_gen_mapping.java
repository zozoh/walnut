package com.site0.walnut.ext.data.titanium.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.titanium.WnI18nService;
import com.site0.walnut.ext.data.titanium.api.TiGenMapping;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.each.WnEachIteratee;

@JvmHdlParamArgs("cqn")
public class ti_gen_mapping implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String as = hc.params.getString("as", "form");
        String fph = hc.params.getString("f");
        String type = hc.params.val(0, "export");
        String forceFieldType = hc.params.getString("fldt", null);
        String getKey = hc.params.getString("get");
        String lang = hc.params.getString("lang", "zh-cn");
        String i18n = hc.params.getString("i18n", "/rs/ti/i18n/");

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
        Object fldo = Json.fromJson(json);
        List<NutMap> fields;
        if (fldo instanceof Map) {
            if (!Ws.isBlank(getKey))
                fldo = Mapl.cell(fldo, getKey);
        }
        int N = Wlang.count(fldo);
        fields = new ArrayList<>(N);
        Wlang.each(fldo, new WnEachIteratee<Object>() {
            @SuppressWarnings("unchecked")
            public void invoke(int index, Object ele, Object src) {
                NutMap map = NutMap.WRAP((Map<String, Object>) ele);
                fields.add(map);
            }
        });

        // 准备多国语言服务
        WnI18nService i18ns = ti_i18n.createI18nService(sys, lang, i18n);

        // 解析字典
        String dictsJson = null;
        String dicts = hc.params.get("dicts");
        if ("true".equals(dicts)) {
            dictsJson = sys.in.readAll();
        }
        // 读取路径
        else if (!Ws.isBlank(dicts)) {
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
        gm.setForceFieldType(forceFieldType);
        gm.setI18ns(i18ns);
        gm.setLang(lang);

        // 输出
        NutMap mapping = gm.genMapping(fields);

        JsonFormat jfmt = Cmds.gen_json_format(hc.params);
        String str;
        if ("export".equals(type)) {
            str = Json.toJson(mapping, jfmt);
        } else {
            str = Json.toJson(Wlang.map("mapping", mapping), jfmt);
        }
        sys.out.println(str);
    }

}
