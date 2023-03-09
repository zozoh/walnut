package org.nutz.walnut.ext.data.titanium.impl;

import org.nutz.lang.util.NutMap;

public class TiGenImportMappingByTable extends TiGenExportMappingByTable {

    @Override
    protected void __join_one_display_key(String key, String ftitle, boolean asDft, NutMap dis) {
        NutMap v = new NutMap();
        v.put("name", key);
        if (asDft) {
            v.put("asDefault", asDft);
        }
        // 记入映射
        this.putFieldMapping(ftitle, v);
    }

}
