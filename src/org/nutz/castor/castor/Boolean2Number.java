package org.nutz.castor.castor;

import org.nutz.castor.Castor;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;

public class Boolean2Number extends Castor<Boolean, Number> {

    @Override
    public Number cast(Boolean src, Class<?> toType, String... args) {
        try {
            return (Number) Mirror    .me(toType)
                                    .getWrapperClass()
                                    .getConstructor(String.class)
                                    .newInstance(src ? "1" : "0");
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e);
        }
    }

}
