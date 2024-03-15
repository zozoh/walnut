package org.nutz.castor.castor;

import org.nutz.castor.Castor;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;

public class String2Boolean extends Castor<String, Boolean> {

    @Override
    public Boolean cast(String src, Class<?> toType, String... args) {
        if (Strings.isBlank(src))
            return false;
        return Wlang.parseBoolean(src);
    }

}
