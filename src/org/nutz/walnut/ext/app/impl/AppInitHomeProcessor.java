package org.nutz.walnut.ext.app.impl;

import org.nutz.lang.util.NutMap;

public class AppInitHomeProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        NutMap meta = ing.genMeta(true);
        ing.io.appendMeta(ing.oDist, meta);
    }

}
