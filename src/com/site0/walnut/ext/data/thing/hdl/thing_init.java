package com.site0.walnut.ext.data.thing.hdl;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("Qf")
public class thing_init implements JvmHdl {

    private static final String DFT_THING_JSON_PATH = thing_init.class.getPackage()
                                                                      .getName()
                                                                      .replace('.', '/')
                                                      + "/dft_thing.json";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        boolean isQ = hc.params.is("Q");

        // 找到集合
        WnObj oTS = Things.checkThingSet(hc.oRefer);

        // 如果 thing 目录木有 icon 标记一个
        if (!oTS.has("icon")) {
            oTS.setv("icon", "fas-cubes");
            sys.io.set(oTS, "^icon$");
            if (!isQ)
                sys.out.printlnf("%-12s: %s", " ++ icon", oTS.getString("icon"));
        }

        // 确保有索引目录
        WnObj oIndex = sys.io.createIfNoExists(oTS, "index", WnRace.DIR);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./index", oIndex.id());

        // 确保有注释目录
        // WnObj oComment = sys.io.createIfNoExists(oTS, "comment", WnRace.DIR);
        // if (!isQ)
        // sys.out.printlnf("%-12s: %s", "./comment", oComment.id());

        // 确保有 data 目录
        WnObj oData = sys.io.createIfNoExists(oTS, "data", WnRace.DIR);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./data", oData.id());

        if (!oData.isType("th_data")) {
            sys.io.set(oData.type("th_data"), "^(tp)$");
        }
        // ---------------------------------------------------
        // 确保有 thing.json
        WnObj oDef = sys.io.createIfNoExists(oTS, "thing.json", WnRace.FILE);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./thing.json", oDef.id());

        // 试图读取一下
        String json = sys.io.readText(oDef);

        // 写入 thing.json 默认内容
        if (!oDef.isLink() && (Strings.isBlank(json) || hc.params.is("f"))) {
            if (!isQ)
                sys.out.println(" >> default write thing.json");

            // 得到默认内容
            String str = Files.read(DFT_THING_JSON_PATH);

            // 写入
            sys.io.writeText(oDef, str);
        }

        // 最后结束
        if (!isQ)
            sys.out.println("All done");

    }

}
