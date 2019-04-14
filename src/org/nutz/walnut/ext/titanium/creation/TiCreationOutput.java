package org.nutz.walnut.ext.titanium.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TiCreationOutput {

    private List<TiTypeInfo> types;

    public void asNone() {
        this.types = null;
    }

    public void asList(int len) {
        this.types = new ArrayList<>(len);
    }

    public List<TiTypeInfo> getTypes() {
        return types;
    }

    public void setTypes(List<TiTypeInfo> types) {
        this.types = types;
    }

    public void addType(TiTypeInfo info) {
        this.types.add(info);
    }

    public void addAllTypes(Collection<TiTypeInfo> infos) {
        this.types.addAll(infos);
    }

}
