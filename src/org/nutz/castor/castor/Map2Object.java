package org.nutz.castor.castor;

import java.util.Map;

import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;
import com.site0.walnut.util.Wlang;

@SuppressWarnings({"rawtypes"})
public class Map2Object extends Castor<Map, Object> {

    @Override
    public Object cast(Map src, Class<?> toType, String... args) throws FailToCastObjectException {
        return Wlang.map2Object(src, toType);
    }

}
