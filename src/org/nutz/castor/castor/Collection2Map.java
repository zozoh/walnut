package org.nutz.castor.castor;

import java.util.Collection;

import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;

@SuppressWarnings("rawtypes")
public class Collection2Map extends Castor<Collection, NutMap> {

    @Override
    public NutMap cast(Collection src, Class<?> toType, String... args)
            throws FailToCastObjectException {
        if (null == args || args.length == 0)
            throw Wlang.makeThrow(FailToCastObjectException.class,
                                  "For the elements in Collection %s, castors don't know which one is the key field.",
                                  src.getClass().getName());
        return Wlang.collection2map(src, args[0]);
    }

}
