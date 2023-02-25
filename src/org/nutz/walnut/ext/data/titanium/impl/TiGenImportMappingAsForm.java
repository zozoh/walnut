package org.nutz.walnut.ext.data.titanium.impl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.titanium.util.TiDict;
import org.nutz.walnut.util.Ws;

public class TiGenImportMappingAsForm extends TiGenExportMappingAsForm {

    @Override
    protected void __join_one_key(String name,
                                  String title,
                                  String type,
                                  boolean asDft,
                                  NutMap field) {

        String comType = getFieldComType(field);
        NutMap comConf = getFieldComConf(field);

        NutMap v = new NutMap();
        v.put("name", name);
        if (!Ws.isBlank(type)) {
            v.put("type", type);
        }
        if (asDft) {
            v.put("asDefault", asDft);
        }
        if (field.has("defaultAs")) {
            v.put("defaultAs", field.get("defaultAs"));
        }
        // 是否记入字典
        if (comConf != null && this.hasDicts()) {
            TiDict dict = getFieldDict(comType, comConf);
            // 处理字典
            if (null != dict) {
                if (dict.isDataAsDynamic()) {
                    v.put("optionsFile", dict.getDynamicDataPath());
                    v.put("optionsFromKey", dict.getTextName());
                    v.put("optionsToKey", dict.getValueName());
                }
                // 静态值映射
                else if (dict.isDataAsList()) {
                    v.put("values", dict.getMappingText2Value());
                }
            }
        }
        // 记入映射
        this.putFieldMapping(title, v);
    }

}
