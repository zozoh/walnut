package org.nutz.castor.castor;

import java.lang.reflect.Array;

import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;
import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;

public class String2Array extends Castor<String, Object> {

    public String2Array() {
        this.fromClass = String.class;
        this.toClass = Array.class;
    }

    @Override
    public Object cast(String src, Class<?> toType, String... args)
            throws FailToCastObjectException {
        if (Strings.isQuoteByIgnoreBlank(src, '[', ']')) {
            return Json.fromJson(toType, src);
        }
        String[] ss = Strings.splitIgnoreBlank(src);
        return Wlang.array2array(ss, toType.getComponentType());
    }

}
