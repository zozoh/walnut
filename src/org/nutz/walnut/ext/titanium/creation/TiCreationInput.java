package org.nutz.walnut.ext.titanium.creation;

import java.util.Map;

public class TiCreationInput {

    private String[] includes;

    private Map<String, String[]> mapping;

    private Map<String, String> types;

    public boolean hasIncludes() {
        return null != includes && includes.length > 0;
    }

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public boolean hasMapping() {
        return null != mapping && mapping.size() > 0;
    }

    public Map<String, String[]> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String[]> mapping) {
        this.mapping = mapping;
    }

    public boolean hasTypes() {
        return null != types && types.size() > 0;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }

}
