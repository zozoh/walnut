package org.nutz.walnut.util.str;

import org.nutz.walnut.util.Ws;

public class WnStrCamelCase implements WnStrCaseConvertor {

    @Override
    public String covert(String input) {
        if (null == input)
            return null;
        return Ws.camelCase(input);
    }

}
