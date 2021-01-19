package org.nutz.walnut.ext.titanium.hdl;

import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class ti_i18n_compare implements JvmHdl {

    private static final String HR = Ws.repeat('-', 40);

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String ph = hc.params.val_check(0);
        WnObj dHome = Wn.checkObj(sys, ph);

        // 指定语言版本
        String lang0 = "zh-cn";
        String lang1 = "en-us";
        String[] langs = hc.params.subvals(1);
        if (langs.length > 1) {
            lang0 = langs[0];
            lang1 = langs[1];
        }

        WnObj dir0 = sys.io.check(dHome, lang0);
        WnObj dir1 = sys.io.check(dHome, lang1);

        // 循环文件
        List<WnObj> files = sys.io.getChildren(dir0, null);

        for (WnObj f0 : files) {
            String fnm = f0.name();
            if (!fnm.endsWith(".i18n.json")) {
                continue;
            }
            sys.out.println(HR);
            sys.out.printlnf("@%s", fnm);

            WnObj f1 = sys.io.fetch(dir1, fnm);

            NutMap map0 = sys.io.readJson(f0, NutMap.class);
            NutMap map1 = sys.io.readJson(f1, NutMap.class);
            int n = Math.abs(map0.size() - map1.size());
            sys.out.printlnf(" Found >> %d diff items", n);

            NutMap all = map0.duplicate();
            all.putAll(map1);

            sys.out.printlnf(" >>> check %s", lang0);
            for (String key : all.keySet()) {
                if (!map0.has(key)) {
                    Object val = map1.get(key);
                    sys.out.printlnf("   \"%s\" : \"%s\",", key, val);
                }
            }

            sys.out.printlnf(" >>> check %s", lang1);
            for (String key : all.keySet()) {
                if (!map1.has(key)) {
                    Object val = map0.get(key);
                    sys.out.printlnf("   \"%s\" : \"%s\",", key, val);
                }
            }
        }
    }

}
