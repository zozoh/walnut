package org.nutz.walnut.util.str;

import org.nutz.walnut.util.Ws;

public class WnStrSnakeCase implements WnStrCaseConvertor {

    @Override
    public String covert(String input) {
        if (null == input)
            return null;
        return Ws.snakeCase(input);
    }

}
