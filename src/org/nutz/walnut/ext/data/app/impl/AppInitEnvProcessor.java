package org.nutz.walnut.ext.data.app.impl;

import java.util.Map;

public class AppInitEnvProcessor implements AppInitProcessor {

    @Override
    public void process(AppInitItemContext ing) {
        if (ing.item.hasMeta()) {
            String cmdText = "";
            for (Map.Entry<String, Object> en : ing.item.getMeta().entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                cmdText += String.format("me -set '%s=%s';\n", key, val);
            }
            ing.run.exec(cmdText);
            ing.printlnf("SETUP ENV BY:\n%s", cmdText);
        }
    }

}
