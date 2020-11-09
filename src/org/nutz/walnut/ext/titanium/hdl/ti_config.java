package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_config implements JvmHdl {

    private static final NutMap dftDictionaries;

    static {
        dftDictionaries = Json.fromJsonFile(NutMap.class,
                                            Files.findFile("org/nutz/walnut/ext/titanium/hdl/wn-dft-dictionaries.json"));
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
