package com.site0.walnut.util.explain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class WnExplainMap implements WnExplain {

    private List<WnExplainPutToMap> pairs;

    public WnExplainMap(Map<String, Object> input) {
        pairs = new ArrayList<>(input.size());
        for (Map.Entry<String, Object> en : input.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            WnExplain ex = WnExplains.parse(val);
            WnExplainPutToMap em = new WnExplainPutToMap(key, ex);
            pairs.add(em);
        }
    }

    @Override
    public Object explain(NutBean context) {
        NutMap re = new NutMap();
        for (WnExplainPutToMap pair : pairs) {
            pair.putTo(context, re);
        }
        return re;
    }

}
