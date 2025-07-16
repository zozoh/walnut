package com.site0.walnut.util.str;

import java.util.HashMap;
import java.util.Map;

public class WnStrCases {

    private static Map<String, WnStrCaseConvertor> covertors = new HashMap<>();
    private static WnStrCaseConvertor dft = new WnStrDefaultCase();

    static {
        covertors.put("camel", new WnStrCamelCase());
        covertors.put("kebab", new WnStrKebabCase());
        covertors.put("snake", new WnStrSnakeCase());
        covertors.put("upper", new WnStrUpperCase());
        covertors.put("lower", new WnStrLowerCase());
        covertors.put("snake_upper", new WnStrSnakeUpperCase());
    }

    public static WnStrCaseConvertor get(String mode) {
        mode = mode.toLowerCase();
        return covertors.get(mode);
    }

    public static WnStrCaseConvertor check(String mode) {
        WnStrCaseConvertor re = covertors.get(mode);
        if (null == re) {
            return dft;
        }
        return re;
    }

}
