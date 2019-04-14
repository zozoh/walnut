package org.nutz.walnut.ext.titanium.creation;

import java.util.LinkedHashMap;

public class TiTypes extends LinkedHashMap<String, TiTypeInfo> {
    public TiTypes clone() {
        TiTypes types = new TiTypes();
        types.putAll(this);
        return types;
    }
}
