package com.site0.walnut.util.str;

import com.site0.walnut.util.Ws;

public class WnStrLowerCase implements WnStrCaseConvertor {

    @Override
    public String covert(String input) {
        if (null == input)
            return null;
        return Ws.toLower(input);
    }

}
