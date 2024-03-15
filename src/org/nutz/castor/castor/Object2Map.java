package org.nutz.castor.castor;

import java.util.Map;

import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;
import com.site0.walnut.util.Wlang;

@SuppressWarnings({"rawtypes"})
public class Object2Map extends Castor<Object, Map> {

    @Override
    public Map cast(Object src, Class<?> toType, String... args) throws FailToCastObjectException {
        return Wlang.obj2map(src);
    }

}
