package com.site0.walnut.util.tmpl.ele;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.util.Ws;
import org.nutz.lang.meta.Pair;

public class StrMappingConvertor implements StrEleConvertor {

    private Map<String, String> mapping;

    StrMappingConvertor(String input) {
        mapping = new HashMap<String, String>();
        String[] ss = Ws.split(input, false, ',');
        for (String s : ss) {
            Pair<String> p = Pair.create(s);
            mapping.put(p.getName(), p.getValue());
        }
    }

    @Override
    public String process(String str) {
        return Ws.sNull(mapping.get(str), str);
    }

}
