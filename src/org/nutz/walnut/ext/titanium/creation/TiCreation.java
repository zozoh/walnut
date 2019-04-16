package org.nutz.walnut.ext.titanium.creation;

import java.util.HashMap;
import java.util.Map;

public class TiCreation {

    private Map<String, String[]> mapping;

    // 键值为 zh-cn / en-us ...
    private Map<String, TiTypes> typesByLang;

    public TiCreation() {
        this.mapping = new HashMap<>();
        this.typesByLang = new HashMap<>();
    }

    public void addMapping(Map<String, String[]> mapping) {
        this.mapping.putAll(mapping);
    }

    public void addAllTypes(Map<String, TiTypes> typesMap) {
        for (Map.Entry<String, TiTypes> en : typesMap.entrySet()) {
            String lang = en.getKey();
            TiTypes tpmap = en.getValue();
            this.addTypes(lang, tpmap);
        }
    }

    public void addTypes(String lang, TiTypes types) {
        if (null == types || types.isEmpty())
            return;

        if (typesByLang.containsKey(lang)) {
            typesByLang.get(lang).putAll(types);
        }
        // 否则新建一个
        else {
            typesByLang.put(lang, types.clone());
        }
    }

    public void mergeWith(TiCreation tic) {
        this.addMapping(tic.mapping);
        this.addAllTypes(tic.typesByLang);
    }

    public TiCreationOutput getOutput(String type, String lang) {
        // 获取映射列表
        String[] tpNames = mapping.get(type);

        // 获取类型集合
        TiTypes types = this.typesByLang.get(lang);

        // 准备返回数据
        TiCreationOutput output = new TiCreationOutput();

        // 组装返回数据
        // 无
        if (null == tpNames) {
            output.asNone();
        }
        // 全部类型
        else if (tpNames.length == 0) {
            output.setFreeCreate(true);
            output.asList(types.size());
            output.addAllTypes(types.values());
        }
        // 选取指定类型
        else {
            output.asList(tpNames.length);
            for (String typeName : tpNames) {
                if ("*".equals(typeName)) {
                    output.setFreeCreate(true);
                    continue;
                }
                TiTypeInfo info = types.get(typeName);
                if (null != info) {
                    output.addType(info);
                }
            }
        }

        // 返回数据
        return output;
    }

}
