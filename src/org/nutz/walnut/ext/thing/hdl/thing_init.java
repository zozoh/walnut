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
        WnObj oTS = Things.checkThingSet(hc.oHome);

        // 确保有 data 目录
        WnObj oData = sys.io.createIfNoExists(oTS, "data", WnRace.DIR);
        if (!isQ)
            sys.out.println("check data : " + oData.id());

        // 确保有 thing.json
        WnObj oDef = sys.io.createIfNoExists(oTS, "thing.js", WnRace.FILE);
        if (!isQ)
            sys.out.println("check thing.js : " + oDef.id());

        // 写入 thing.json 默认内容
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
