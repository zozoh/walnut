package org.nutz.dao.impl.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nutz.dao.sql.VarSet;
import com.site0.walnut.util.Wlang;

class SimpleVarSet implements VarSet {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map;

    SimpleVarSet() {
        this.map = new HashMap<String, Object>();
    }

    public VarSet set(String name, Object value) {
        map.put(name, value);
        return this;
    }

    public Object get(String name) {
        return map.get(name);
    }

    public Set<String> keys() {
        return map.keySet();
    }

    public VarSet putAll(Map<String, Object> map) {
        if (map != null) {
            this.map.putAll(map);
        }
        return this;
    }

    public VarSet putAll(Object pojo) {
        if (pojo != null) {
            Map<String, Object> pojoMap = Wlang.obj2map(pojo);
            this.map.putAll(pojoMap);
        }
        return this;
    }

    public int size() {
        return map.size();
    }
}
