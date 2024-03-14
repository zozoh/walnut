package com.site0.walnut.ext.data.app.impl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class AppInitFileProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        WnObj obj = ing.checkObj(WnRace.FILE);

        NutMap meta = ing.genMeta(true);

        ing.io.appendMeta(obj, meta);
        ing.writeFile(obj);
    }

}
