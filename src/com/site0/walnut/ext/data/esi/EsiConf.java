package com.site0.walnut.ext.data.esi;

import java.util.Map;

public class EsiConf {

    // 索引名称
    private String name;
    // 映射列表
    private Map<String, EsiMappingField> mapping;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, EsiMappingField> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, EsiMappingField> mapping) {
        this.mapping = mapping;
    }

}
