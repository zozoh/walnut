package org.nutz.walnut.ext.adv.app.impl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public class AppInitDirProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        WnObj obj = ing.checkObj(WnRace.DIR);
        
        NutMap meta = ing.genMeta(true);
        
        ing.io.appendMeta(obj, meta);
    }

}
