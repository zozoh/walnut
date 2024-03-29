package org.nutz.castor.castor;

import java.lang.reflect.Array;
import java.util.Map;

import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;
import com.site0.walnut.util.Wlang;

@SuppressWarnings({"rawtypes"})
public class Map2Array extends Castor<Map, Object> {

    public Map2Array() {
        this.fromClass = Map.class;
        this.toClass = Array.class;
    }

    @Override
    public Object cast(Map src, Class<?> toType, String... args) throws FailToCastObjectException {
        return Wlang.collection2array(src.values(), toType.getComponentType());
    }

}
