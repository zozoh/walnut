package com.site0.walnut.ext.data.titanium.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_config implements JvmHdl {

    private static final String DICT_JSON_PH = "com/site0/walnut/ext/data/titanium/hdl/wn-dft-dictionaries.json";
    private static final NutMap dftDictionaries;

    static {
        dftDictionaries = Json.fromJsonFile(NutMap.class, Files.findFile(DICT_JSON_PH));
        // 附加一个内置字段的字典
        String ph = "com/site0/walnut/core/indexer/dao/built-fields.json";
        NutMap[] list = Json.fromJsonFile(NutMap[].class, Files.findFile(ph));
        List<NutMap> fdata = new ArrayList<>(list.length);
        for (NutMap li : list) {
            NutMap field = new NutMap();
            int width = li.getInt("width", 0);
            if (width > 0) {
                li.put("widthText", "[" + width + "]");
            }
            String text = WnTmpl.exec("${name} : ${type} : ${columnType}${widthText?}", li);
            String value = li.getString("name");
            field.put("value", value);
            field.put("text", text);
            fdata.add(field);
        }
        dftDictionaries.put("BuiltInFields", Wlang.map("data", fdata));
    }

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String name = hc.params.get("name", "config.json");
        String aph = "~/.ti/" + name;
        WnObj oConfig = Wn.getObj(sys, aph);
        if (null == oConfig) {
            sys.out.println("{}");
        }
        // 解析
        else {
            NutMap map = sys.io.readJson(oConfig, NutMap.class);

            // 加入系统默认的字典
            NutMap dicts = map.getAs("dictionary", NutMap.class);
            if (null == dicts) {
                dicts = new NutMap();
                map.put("dictionary", dicts);
            }
            dicts.putAll(dftDictionaries);

            // 输出
            String json = Json.toJson(map, hc.jfmt);
            sys.out.print(json);
        }
    }

}
