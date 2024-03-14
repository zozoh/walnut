package com.site0.walnut.ext.data.titanium.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TiCreation {

    private boolean freeCreate;

    private String[] typeNames;

    private List<TiCreateType> types;

    public boolean isFreeCreate() {
        return freeCreate;
    }

    public void setFreeCreate(boolean freeCreate) {
        this.freeCreate = freeCreate;
    }

    public boolean hasTypeNames() {
        return null != typeNames && typeNames.length > 0;
    }

    public String[] getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(String[] typeNames) {
        this.typeNames = typeNames;
    }

    public void loadTypes(Map<String, TiCreateType> dict) {
        List<TiCreateType> list = new ArrayList<>(typeNames.length);
        for (String typeName : typeNames) {
            TiCreateType tct = dict.get(typeName);
            if (null != tct) {
                list.add(tct);
            }
        }
        this.types = list;
    }

    public List<TiCreateType> getTypes() {
        return types;
    }

    public void setTypes(List<TiCreateType> types) {
        this.types = types;
    }

}
