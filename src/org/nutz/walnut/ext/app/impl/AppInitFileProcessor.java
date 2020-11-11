package org.nutz.walnut.ext.app.impl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class AppInitFileProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        WnObj obj = ing.checkObj(WnRace.FILE);

        NutMap meta = ing.genMeta(true);

        ing.io.appendMeta(obj, meta);
        ing.writeFile(obj);
    }

}
