package com.site0.walnut.ext.data.app.impl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

public class AppInitHomeProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        NutMap meta = ing.genMeta(true);
        String json = Json.toJson(meta, JsonFormat.nice());
        ing.io.appendMeta(ing.oDist, meta);
        ing.printlnf("SETUP HOME BY:\n%s", json);
    }

}
