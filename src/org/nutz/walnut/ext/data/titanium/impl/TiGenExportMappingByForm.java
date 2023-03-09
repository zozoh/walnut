package org.nutz.walnut.ext.data.titanium.impl;

import java.util.Collection;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.titanium.api.TiGenMapping;
import org.nutz.walnut.ext.data.titanium.util.TiDict;
import org.nutz.walnut.util.Ws;

public class TiGenExportMappingByForm extends TiGenMapping {

    @Override
    protected void joinField(NutMap field, String forceFieldType) {
        String title = field.getString("title");
        if (Ws.isBlank(title)) {
            return;
        }
        title = this.traslateText(title);

        String name = field.getString("name");
        // 纯标题
        if (Ws.isBlank(name)) {
            this.putFieldMapping(title, "----------");
        }
        // 字段
        else {
            String type = field.getString("type");
            if (null != forceFieldType && null != type) {
                type = forceFieldType;
            }

            // 记入映射
            boolean required = field.getBoolean("required");
            boolean asDft = this.isDefault(name, required);

            __join_one_key(name, title, type, asDft, field);
        }

        // 递归
        List<NutMap> children = field.getAsList("fields", NutMap.class);
        if (null != children) {
            for (NutMap sub : children) {
                joinField(sub, forceFieldType);
            }
        }
    }

    protected void __join_one_key(String name,
                                  String title,
                                  String type,
                                  boolean asDft,
                                  NutMap field) {

        String comType = getFieldComType(field);
        NutMap comConf = getFieldComConf(field);

        NutMap v = new NutMap();
        v.put("name", title);
        if (!Ws.isBlank(type)) {
            if ("AMS".equals(type) || "TiInputDateTime".equals(type)) {
                type = "DateTime";
                v.put("format", "yyyy-MM-dd HH:mm:ss");
            }
            if ("TiInputDate".equals(type)) {
                type = "DateTime";
                v.put("format", "yyyy-MM-dd");
            }
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
                    v.put("optionsFromKey", dict.getValueName());
                    v.put("optionsToKey", dict.getTextName());
                }
                // 静态值映射
                else if (dict.isDataAsList()) {
                    v.put("values", dict.getMappingValue2Text());
                }
            }
        }
        // 记入映射
        this.putFieldMapping(name, v);
    }

    protected TiDict getFieldDict(String comType, NutMap comConf) {
        TiDict dict = null;
        if ("TiLabel".equals(comType) || Ws.isBlank(comType)) {
            String dictName = comConf.getString("dict");
            dict = this.getDict(dictName);
        }
        // options
        else if (comConf.has("options")) {
            Object options = comConf.get("options");
            if (null != options) {
                // 采用字典映射
                if (options instanceof String) {
                    dict = this.getDict(options.toString());
                }
                // 静态字典
                else if (options instanceof Collection<?>) {
                    dict = new TiDict((Collection<?>) options);
                }
            }
        }
        return dict;
    }

    protected NutMap getFieldComConf(NutMap field) {
        return field.getAs("comConf", NutMap.class);
    }

    protected String getFieldComType(NutMap field) {
        String comType = field.getString("comType", "TiLabel");
        comType = Ws.kebabCase(comType);
        comType = Ws.camelCase(comType);
        return Ws.upperFirst(comType);
    }

}
