package org.nutz.walnut.ext.thing.hdl;

import org.nutz.lang.Files;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("Qf")
public class thing_init implements JvmHdl {

    private static final String DFT_THING_JS_PATH = thing_init.class.getPackage()
                                                                    .getName()
                                                                    .replace('.', '/')
                                                    + "/dft_thing.js";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        boolean isQ = hc.params.is("Q");

        // 找到集合
        WnObj oTS = Things.checkThingSet(hc.oRefer);

        // 确保有索引目录
        WnObj oIndex = sys.io.createIfNoExists(oTS, "index", WnRace.DIR);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./index", oIndex.id());

        // 确保有注释目录
        WnObj oComment = sys.io.createIfNoExists(oTS, "comment", WnRace.DIR);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./comment", oComment.id());

        // 确保有 data 目录
        WnObj oData = sys.io.createIfNoExists(oTS, "data", WnRace.DIR);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./data", oData.id());

        if (!oData.isType("th_data")) {
            sys.io.set(oData.type("th_data"), "^(tp)$");
        }

        // 确保有 thing.js
        WnObj oDef = sys.io.createIfNoExists(oTS, "thing.js", WnRace.FILE);
        if (!isQ)
            sys.out.printlnf("%-12s: %s", "./thing.js", oDef.id());

        // 写入 thing.js 默认内容
        if (oDef.len() == 0 || hc.params.is("f")) {
            if (!isQ)
                sys.out.println(" >> default write thing.js");

            // 得到默认内容
            String str = Files.read(DFT_THING_JS_PATH);

            // 写入
            sys.io.writeText(oDef, str);
        }

        // 最后结束
        if (!isQ)
            sys.out.println("All done");

    }

}
